package travel.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import travel.exception.BookingException;
import travel.exception.FlightNotFoundException;
import travel.exception.HotelNotFoundException;
import travel.exception.NoRoomsAvailableException;
import travel.exception.ReservationNotFoundException;
import travel.model.Flight;
import travel.model.FlightReservation;
import travel.model.FlightSearchCriteria;
import travel.model.Hotel;
import travel.model.HotelReservation;
import travel.model.HotelSearchCriteria;
import travel.model.Reservation;

/**
 * Logika inti aplikasi pemesanan. Kelas ini tidak melakukan I/O konsol sama sekali,
 * sehingga bisa diuji langsung dan dipakai ulang oleh antarmuka lain (mis. web/GUI).
 *
 * <p>Menyimpan tiga koleksi: katalog penerbangan, katalog hotel, dan reservasi aktif.
 * {@link Clock} disuntik lewat constructor supaya logika tanggal deterministik saat diuji.
 */
public class TravelApp {

    /** Urutan default hasil pencarian penerbangan: termurah dulu, lalu paling pagi. */
    public static final Comparator<Flight> CHEAPEST_FIRST =
            Comparator.comparingLong(Flight::getPrice).thenComparing(Flight::getDepartureTime);

    /** Urutan alternatif: jam berangkat paling awal dulu. */
    public static final Comparator<Flight> EARLIEST_FIRST =
            Comparator.comparing(Flight::getDepartureTime).thenComparingLong(Flight::getPrice);

    private final Clock clock;
    private final List<Flight> flights;
    private final List<Hotel> hotels;
    private final List<Reservation> reservations = new ArrayList<>();

    public TravelApp(Clock clock, List<Flight> flights, List<Hotel> hotels) {
        this.clock = Objects.requireNonNull(clock);
        this.flights = new ArrayList<>(flights);
        this.hotels = new ArrayList<>(hotels);
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    // =====================================================================
    // Penerbangan
    // =====================================================================

    public List<Flight> searchFlights(FlightSearchCriteria criteria) {
        return searchFlights(criteria, CHEAPEST_FIRST);
    }

    /**
     * Mencari penerbangan yang cocok dengan rute, tanggal, dan kursi yang cukup.
     * Penyaringan memakai stream + lambda.
     */
    public List<Flight> searchFlights(FlightSearchCriteria criteria, Comparator<Flight> order) {
        requireNotInPast(criteria.date());
        return flights.stream()
                .filter(f -> f.getOrigin().equalsIgnoreCase(criteria.origin()))
                .filter(f -> f.getDestination().equalsIgnoreCase(criteria.destination()))
                .filter(f -> f.getDate().equals(criteria.date()))
                .filter(f -> f.hasSeats(criteria.passengers()))
                .sorted(order)
                .toList();
    }

    /** Saran tanggal lain (maks. 5) yang masih punya penerbangan untuk rute dan jumlah penumpang yang sama. */
    public List<LocalDate> alternativeFlightDates(FlightSearchCriteria criteria) {
        return flights.stream()
                .filter(f -> f.getOrigin().equalsIgnoreCase(criteria.origin()))
                .filter(f -> f.getDestination().equalsIgnoreCase(criteria.destination()))
                .filter(f -> f.hasSeats(criteria.passengers()))
                .map(Flight::getDate)
                .filter(d -> !d.isBefore(today()))
                .distinct()
                .sorted()
                .limit(5)
                .toList();
    }

    /** Mencari penerbangan berdasarkan nomor di antara hasil pencarian. */
    public Flight findFlight(String flightNumber, FlightSearchCriteria criteria) throws FlightNotFoundException {
        String wanted = flightNumber.trim();
        return searchFlights(criteria).stream()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new FlightNotFoundException(wanted));
    }

    /** Membuat reservasi penerbangan: kursi dikurangi, nomor konfirmasi dibuat, reservasi disimpan. */
    public FlightReservation bookFlight(Flight flight, List<String> passengerNames, String contact)
            throws BookingException {
        Objects.requireNonNull(flight, "flight");
        if (passengerNames == null || passengerNames.isEmpty()) {
            throw new IllegalArgumentException("Minimal harus ada satu penumpang.");
        }
        flight.reserveSeats(passengerNames.size()); // dilempar InsufficientSeatsException bila kurang

        int confirmation = ConfirmationGenerator.generate(usedConfirmationNumbers());
        Flight snapshot = flight.bookedCopy(passengerNames.size(), confirmation);
        FlightReservation reservation = new FlightReservation(
                confirmation, contact, LocalDateTime.now(clock), flight, snapshot, passengerNames);
        reservations.add(reservation);
        return reservation;
    }

    // =====================================================================
    // Hotel
    // =====================================================================

    /**
     * Mencari hotel di kota yang diminta dengan kamar kosong cukup di seluruh rentang tanggal.
     * Diurutkan dari harga termurah.
     */
    public List<Hotel> searchHotels(HotelSearchCriteria criteria) {
        requireNotInPast(criteria.checkIn());
        return hotels.stream()
                .filter(h -> h.getLocation().equalsIgnoreCase(criteria.location()))
                .filter(h -> availableRooms(h, criteria.checkIn(), criteria.checkOut())
                        >= h.roomsNeededFor(criteria.guests()))
                .sorted(Comparator.comparingLong(Hotel::getPrice))
                .toList();
    }

    /**
     * Sisa kamar sebuah hotel pada rentang [checkIn, checkOut). Dua rentang bertabrakan bila
     * keduanya saling tumpang tindih; check-out pada hari yang sama dengan check-in tamu lain
     * dianggap tidak bertabrakan.
     */
    public int availableRooms(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
        int occupied = reservations.stream()
                .filter(HotelReservation.class::isInstance)
                .map(HotelReservation.class::cast)
                .filter(r -> r.getHotel().getHotelId().equals(hotel.getHotelId()))
                .filter(r -> checkIn.isBefore(r.getHotel().getCheckOut())
                        && checkOut.isAfter(r.getHotel().getCheckIn()))
                .mapToInt(HotelReservation::getRooms)
                .sum();
        return hotel.getTotalRooms() - occupied;
    }

    /** Mencari hotel berdasarkan ID di antara hasil pencarian. */
    public Hotel findHotel(String hotelId, HotelSearchCriteria criteria) throws HotelNotFoundException {
        String wanted = hotelId.trim();
        return searchHotels(criteria).stream()
                .filter(h -> h.getHotelId().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new HotelNotFoundException(wanted));
    }

    public HotelReservation bookHotel(Hotel hotel, HotelSearchCriteria criteria, String guestName, String contact)
            throws BookingException {
        Objects.requireNonNull(hotel, "hotel");
        // Cek ulang ketersediaan tepat sebelum menyimpan (data bisa berubah sejak pencarian).
        if (availableRooms(hotel, criteria.checkIn(), criteria.checkOut()) < hotel.roomsNeededFor(criteria.guests())) {
            throw new NoRoomsAvailableException(hotel.getName());
        }
        int confirmation = ConfirmationGenerator.generate(usedConfirmationNumbers());
        Hotel snapshot = hotel.bookedCopy(criteria.checkIn(), criteria.checkOut(), criteria.guests(), confirmation);
        HotelReservation reservation = new HotelReservation(
                confirmation, guestName, contact, LocalDateTime.now(clock), snapshot);
        reservations.add(reservation);
        return reservation;
    }

    // =====================================================================
    // Reservasi
    // =====================================================================

    public Reservation findReservation(int confirmationNumber) throws ReservationNotFoundException {
        for (Reservation reservation : reservations) {
            if (reservation.getConfirmationNumber() == confirmationNumber) {
                return reservation;
            }
        }
        throw new ReservationNotFoundException(confirmationNumber);
    }

    /**
     * Membatalkan reservasi berdasarkan nomor konfirmasi. Reservasi dihapus dari daftar, sumber
     * dayanya dilepas lewat {@link Reservation#cancel()} (polimorfik), lalu pesan sukses dibuat
     * dengan pattern matching {@code instanceof}.
     */
    public String cancelReservation(int confirmationNumber) throws ReservationNotFoundException {
        Reservation target = findReservation(confirmationNumber);
        reservations.remove(target);
        String releaseNote = target.cancel();

        if (target instanceof FlightReservation fr) {
            return "Reservasi PENERBANGAN " + fr.getFlight().getFlightNumber()
                    + " (No. " + confirmationNumber + ") berhasil dibatalkan. " + releaseNote;
        } else if (target instanceof HotelReservation hr) {
            return "Reservasi HOTEL " + hr.getHotel().getName()
                    + " (No. " + confirmationNumber + ") berhasil dibatalkan. " + releaseNote;
        }
        // Tidak tercapai: Reservation sealed dan hanya punya dua subclass di atas.
        throw new IllegalStateException("Tipe reservasi tidak dikenal: " + target.getClass());
    }

    /** Semua reservasi aktif, urut dari yang paling awal dipesan. */
    public List<Reservation> getReservations() {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getBookedAt))
                .toList();
    }

    /** Jumlah reservasi per jenis (Penerbangan/Hotel). */
    public Map<String, Long> countByType() {
        return reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getTypeLabel, TreeMap::new, Collectors.counting()));
    }

    public long totalSpent() {
        return reservations.stream().mapToLong(Reservation::getTotalPrice).sum();
    }

    // =====================================================================
    // Helper
    // =====================================================================

    private Set<Integer> usedConfirmationNumbers() {
        Set<Integer> used = new HashSet<>();
        for (Reservation reservation : reservations) {
            used.add(reservation.getConfirmationNumber());
        }
        return used;
    }

    private void requireNotInPast(LocalDate date) {
        if (date.isBefore(today())) {
            throw new IllegalArgumentException("Tanggal tidak boleh di masa lalu (hari ini " + today() + ").");
        }
    }
}
