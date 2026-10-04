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

public class TravelApp {

    public static final Comparator<Flight> CHEAPEST_FIRST =
            Comparator.comparingLong(Flight::getPrice).thenComparing(Flight::getDepartureTime);

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

    public List<Flight> searchFlights(FlightSearchCriteria criteria) {
        return searchFlights(criteria, CHEAPEST_FIRST);
    }

    public List<Flight> searchFlights(FlightSearchCriteria criteria, Comparator<Flight> order) {
        requireNotInPast(criteria.date());
        LocalDateTime now = LocalDateTime.now(clock);
        return flights.stream()
                .filter(f -> f.getOrigin().equalsIgnoreCase(criteria.origin()))
                .filter(f -> f.getDestination().equalsIgnoreCase(criteria.destination()))
                .filter(f -> f.getDate().equals(criteria.date()))
                .filter(f -> f.getDepartureDateTime().isAfter(now))
                .filter(f -> f.hasSeats(criteria.passengers()))
                .sorted(order)
                .toList();
    }

    public List<LocalDate> alternativeFlightDates(FlightSearchCriteria criteria) {
        LocalDateTime now = LocalDateTime.now(clock);
        return flights.stream()
                .filter(f -> f.getOrigin().equalsIgnoreCase(criteria.origin()))
                .filter(f -> f.getDestination().equalsIgnoreCase(criteria.destination()))
                .filter(f -> f.getDepartureDateTime().isAfter(now))
                .filter(f -> f.hasSeats(criteria.passengers()))
                .map(Flight::getDate)
                .distinct()
                .sorted()
                .limit(5)
                .toList();
    }

    public Flight findFlight(String flightNumber, FlightSearchCriteria criteria) throws FlightNotFoundException {
        String wanted = flightNumber.trim();
        return searchFlights(criteria).stream()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new FlightNotFoundException(wanted));
    }

    public FlightReservation bookFlight(Flight flight, List<String> passengerNames, String contact)
            throws BookingException {
        Objects.requireNonNull(flight, "flight");
        if (passengerNames == null || passengerNames.isEmpty()) {
            throw new IllegalArgumentException("Minimal harus ada satu penumpang.");
        }
        flight.reserveSeats(passengerNames.size());

        int confirmation = ConfirmationGenerator.generate(usedConfirmationNumbers());
        Flight snapshot = flight.bookedCopy(passengerNames.size(), confirmation);
        FlightReservation reservation = new FlightReservation(
                confirmation, contact, LocalDateTime.now(clock), flight, snapshot, passengerNames);
        reservations.add(reservation);
        return reservation;
    }

    public List<Hotel> searchHotels(HotelSearchCriteria criteria) {
        requireNotInPast(criteria.checkIn());
        return hotels.stream()
                .filter(h -> h.getLocation().equalsIgnoreCase(criteria.location()))
                .filter(h -> availableRooms(h, criteria.checkIn(), criteria.checkOut())
                        >= h.roomsNeededFor(criteria.guests()))
                .sorted(Comparator.comparingLong(Hotel::getPrice))
                .toList();
    }

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

    public Reservation findReservation(int confirmationNumber) throws ReservationNotFoundException {
        for (Reservation reservation : reservations) {
            if (reservation.getConfirmationNumber() == confirmationNumber) {
                return reservation;
            }
        }
        throw new ReservationNotFoundException(confirmationNumber);
    }

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
        throw new IllegalStateException("Tipe reservasi tidak dikenal: " + target.getClass());
    }

    public List<Reservation> getReservations() {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getBookedAt))
                .toList();
    }

    public Map<String, Long> countByType() {
        return reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getTypeLabel, TreeMap::new, Collectors.counting()));
    }

    public long totalSpent() {
        return reservations.stream().mapToLong(Reservation::getTotalPrice).sum();
    }

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
