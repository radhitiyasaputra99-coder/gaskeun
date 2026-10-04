package travel.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import travel.exception.BookingException;
import travel.exception.FlightNotFoundException;
import travel.exception.HotelNotFoundException;
import travel.exception.ReservationNotFoundException;
import travel.model.Flight;
import travel.model.FlightReservation;
import travel.model.FlightSearchCriteria;
import travel.model.Hotel;
import travel.model.HotelReservation;
import travel.model.HotelSearchCriteria;
import travel.model.Reservation;
import travel.service.TravelApp;
import travel.util.CityDirectory;
import travel.util.ConsoleStyle;
import travel.util.CurrencyFormat;
import travel.util.DateFormats;

public class ConsoleMenu {

    private final TravelApp app;
    private final ConsoleInput input;
    private final PrintStream out;

    public ConsoleMenu(TravelApp app, InputStream in, PrintStream out) {
        this.app = app;
        this.out = out;
        this.input = new ConsoleInput(in, out);
    }

    public void run() {
        printBanner();
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = input.promptInt("Pilih menu (0-6)", 0, 6);
                switch (choice) {
                    case 1 -> flightFlow(false);
                    case 2 -> hotelFlow(false);
                    case 3 -> flightFlow(true);
                    case 4 -> hotelFlow(true);
                    case 5 -> cancelFlow();
                    case 6 -> viewAllFlow();
                    case 0 -> running = false;
                    default -> out.println("! Pilihan tidak tersedia.");
                }
            }
            out.println();
            out.println("Terima kasih telah menjelajah bareng NusaGo. Selamat jalan-jalan!");
        } catch (InputClosedException e) {
            out.println();
            out.println("Input ditutup. Program berhenti.");
        } finally {
            input.close();
        }
    }

    private void printBanner() {
        out.println(ConsoleStyle.box("NUSAGO - Jelajahi Nusantara!", List.of(
                "Sistem pemesanan penerbangan & hotel berbasis konsol.",
                "Data demo: jadwal penerbangan 30 hari ke depan mulai "
                        + app.today().format(DateFormats.ISO) + ".")));
    }

    private void printMenu() {
        out.println();
        out.println("----- MENU UTAMA -----");
        out.println(" 1. Cari Penerbangan");
        out.println(" 2. Cari Hotel");
        out.println(" 3. Pesan Penerbangan");
        out.println(" 4. Pesan Hotel");
        out.println(" 5. Batalkan Reservasi");
        out.println(" 6. Lihat Semua Pemesanan");
        out.println(" 0. Keluar");
    }

    private void flightFlow(boolean bookNow) {
        out.println();
        out.println("=== " + (bookNow ? "PEMESANAN" : "PENCARIAN") + " PENERBANGAN ===");
        out.println("Kota tersedia: " + CityDirectory.describeAll());

        while (true) {
            String origin = input.promptCity("Masukkan kota asal");
            String destination = input.promptCity("Masukkan kota tujuan");
            LocalDate date = input.promptDate("Masukkan tanggal perjalanan (yyyy-MM-dd)", app.today());
            int passengers = input.promptInt("Masukkan jumlah penumpang (1-" + FlightSearchCriteria.MAX_PASSENGERS + ")",
                    1, FlightSearchCriteria.MAX_PASSENGERS);

            FlightSearchCriteria criteria;
            List<Flight> results;
            try {
                criteria = new FlightSearchCriteria(origin, destination, date, passengers);
                results = app.searchFlights(criteria);
            } catch (IllegalArgumentException e) {
                out.println("! " + e.getMessage());
                if (!input.confirm("Coba lagi")) {
                    return;
                }
                continue;
            }

            if (results.isEmpty()) {
                out.println();
                out.println("Maaf, tidak ada penerbangan tersedia untuk rute, tanggal, dan jumlah penumpang tersebut.");
                List<LocalDate> alternatives = app.alternativeFlightDates(criteria);
                if (!alternatives.isEmpty()) {
                    out.println("Tanggal lain yang masih tersedia untuk rute ini: " + alternatives.stream()
                            .map(d -> d.format(DateFormats.ISO))
                            .collect(Collectors.joining(", ")));
                }
                if (!input.confirm("Coba cari lagi dengan kriteria lain")) {
                    return;
                }
                continue;
            }

            printFlightTable(results, criteria);
            if (bookNow || input.confirm("Pesan salah satu penerbangan ini sekarang")) {
                bookFlight(criteria);
            }
            return;
        }
    }

    private void printFlightTable(List<Flight> results, FlightSearchCriteria c) {
        out.println();
        out.printf("Penerbangan %s (%s) -> %s (%s) | %s | %d penumpang | urut harga termurah%n",
                c.origin(), CityDirectory.codeOf(c.origin()),
                c.destination(), CityDirectory.codeOf(c.destination()),
                c.date().format(DateFormats.DATE), c.passengers());

        List<String[]> rows = results.stream()
                .map(f -> new String[]{
                        f.getFlightNumber(),
                        f.getAirline(),
                        f.getDepartureTime().format(DateFormats.TIME),
                        f.getArrivalLabel(),
                        f.getDurationLabel(),
                        CurrencyFormat.rupiah(f.getPrice()),
                        CurrencyFormat.rupiah(f.getPrice() * c.passengers()),
                        String.valueOf(f.getSeatsAvailable())})
                .toList();
        out.println(ConsoleStyle.table(
                new String[]{"No. Penerbangan", "Maskapai", "Berangkat", "Tiba", "Durasi", "Harga/orang", "Total", "Kursi"},
                rows, 5, 6, 7));
    }

    private void bookFlight(FlightSearchCriteria criteria) {
        Flight chosen = null;
        while (chosen == null) {
            String number = input.readLine("Masukkan nomor penerbangan yang dipesan (kosongkan untuk batal)");
            if (number.isEmpty()) {
                out.println("Pemesanan dibatalkan.");
                return;
            }
            try {
                chosen = app.findFlight(number, criteria);
            } catch (FlightNotFoundException e) {
                out.println("! " + e.getMessage());
            }
        }

        out.println();
        out.println("--- Data Penumpang ---");
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= criteria.passengers(); i++) {
            names.add(input.promptName("Nama lengkap penumpang " + i));
        }
        String contact = input.promptContact("Kontak (no. HP atau email)");

        out.println();
        out.println(ConsoleStyle.box("RINGKASAN PESANAN", List.of(
                "Penerbangan : " + chosen.getFlightNumber() + " - " + chosen.getAirline(),
                "Rute        : " + chosen.getOrigin() + " -> " + chosen.getDestination(),
                "Berangkat   : " + chosen.getDate().format(DateFormats.DATE) + " "
                        + chosen.getDepartureTime().format(DateFormats.TIME),
                "Penumpang   : " + String.join(", ", names),
                "Total bayar : " + CurrencyFormat.rupiah(chosen.getPrice() * names.size()))));
        if (!input.confirm("Konfirmasi pemesanan")) {
            out.println("Pemesanan dibatalkan.");
            return;
        }

        try {
            FlightReservation reservation = app.bookFlight(chosen, names, contact);
            out.println();
            out.println("Pemesanan berhasil! Simpan nomor konfirmasi Anda: " + reservation.getConfirmationNumber());
            reservation.display(out);
        } catch (BookingException e) {
            out.println("! Pemesanan gagal: " + e.getMessage());
        }
    }

    private void hotelFlow(boolean bookNow) {
        out.println();
        out.println("=== " + (bookNow ? "PEMESANAN" : "PENCARIAN") + " HOTEL ===");
        out.println("Kota tersedia: " + CityDirectory.describeAll());

        while (true) {
            String location = input.promptCity("Masukkan kota (lokasi)");
            LocalDate checkIn = input.promptDate("Masukkan tanggal check-in (yyyy-MM-dd)", app.today());
            LocalDate checkOut = input.promptDate("Masukkan tanggal check-out (yyyy-MM-dd)", checkIn.plusDays(1));
            int guests = input.promptInt("Masukkan jumlah tamu (1-" + HotelSearchCriteria.MAX_GUESTS + ")",
                    1, HotelSearchCriteria.MAX_GUESTS);

            HotelSearchCriteria criteria;
            List<Hotel> results;
            try {
                criteria = new HotelSearchCriteria(location, checkIn, checkOut, guests);
                results = app.searchHotels(criteria);
            } catch (IllegalArgumentException e) {
                out.println("! " + e.getMessage());
                if (!input.confirm("Coba lagi")) {
                    return;
                }
                continue;
            }

            if (results.isEmpty()) {
                out.println();
                out.println("Maaf, tidak ada hotel tersedia di " + location + " untuk tanggal dan jumlah tamu tersebut.");
                if (!input.confirm("Coba cari lagi dengan kriteria lain")) {
                    return;
                }
                continue;
            }

            printHotelTable(results, criteria);
            if (bookNow || input.confirm("Pesan salah satu hotel ini sekarang")) {
                bookHotel(criteria);
            }
            return;
        }
    }

    private void printHotelTable(List<Hotel> results, HotelSearchCriteria c) {
        out.println();
        out.printf("Hotel di %s | %s s/d %s (%d malam) | %d tamu | urut harga termurah%n",
                c.location(), c.checkIn().format(DateFormats.DATE), c.checkOut().format(DateFormats.DATE),
                c.nights(), c.guests());

        List<String[]> rows = results.stream()
                .map(h -> new String[]{
                        h.getHotelId(),
                        h.getName(),
                        h.getStarsLabel(),
                        CurrencyFormat.rupiah(h.getPricePerNight()),
                        h.roomsNeededFor(c.guests()) + " kamar",
                        CurrencyFormat.rupiah(h.stayPrice(c.checkIn(), c.checkOut(), c.guests())),
                        String.valueOf(app.availableRooms(h, c.checkIn(), c.checkOut()))})
                .toList();
        out.println(ConsoleStyle.table(
                new String[]{"ID", "Nama Hotel", "Bintang", "Harga/malam", "Kebutuhan", "Total", "Sisa kamar"},
                rows, 3, 5, 6));
    }

    private void bookHotel(HotelSearchCriteria criteria) {
        Hotel chosen = null;
        while (chosen == null) {
            String id = input.readLine("Masukkan ID hotel yang dipesan (kosongkan untuk batal)");
            if (id.isEmpty()) {
                out.println("Pemesanan dibatalkan.");
                return;
            }
            try {
                chosen = app.findHotel(id, criteria);
            } catch (HotelNotFoundException e) {
                out.println("! " + e.getMessage());
            }
        }

        out.println();
        out.println("--- Data Tamu ---");
        String guestName = input.promptName("Nama lengkap tamu utama");
        String contact = input.promptContact("Kontak (no. HP atau email)");

        out.println();
        out.println(ConsoleStyle.box("RINGKASAN PESANAN", List.of(
                "Hotel       : " + chosen.getName() + " (" + chosen.getStarsLabel() + ")",
                "Lokasi      : " + chosen.getLocation(),
                "Menginap    : " + criteria.checkIn().format(DateFormats.DATE) + " s/d "
                        + criteria.checkOut().format(DateFormats.DATE) + " (" + criteria.nights() + " malam)",
                "Tamu/Kamar  : " + criteria.guests() + " tamu / " + chosen.roomsNeededFor(criteria.guests()) + " kamar",
                "Total bayar : " + CurrencyFormat.rupiah(
                        chosen.stayPrice(criteria.checkIn(), criteria.checkOut(), criteria.guests())))));
        if (!input.confirm("Konfirmasi pemesanan")) {
            out.println("Pemesanan dibatalkan.");
            return;
        }

        try {
            HotelReservation reservation = app.bookHotel(chosen, criteria, guestName, contact);
            out.println();
            out.println("Pemesanan berhasil! Simpan nomor konfirmasi Anda: " + reservation.getConfirmationNumber());
            reservation.display(out);
        } catch (BookingException e) {
            out.println("! Pemesanan gagal: " + e.getMessage());
        }
    }

    private void cancelFlow() {
        out.println();
        out.println("=== PEMBATALAN RESERVASI ===");
        int number = input.promptInt("Masukkan nomor konfirmasi (6 digit)", 100_000, 999_999);
        try {
            Reservation reservation = app.findReservation(number);
            reservation.display(out);
            if (input.confirm("Yakin ingin membatalkan reservasi ini")) {
                out.println(app.cancelReservation(number));
            } else {
                out.println("Pembatalan dibatalkan. Reservasi tetap aktif.");
            }
        } catch (ReservationNotFoundException e) {
            out.println("! " + e.getMessage());
        }
    }

    private void viewAllFlow() {
        out.println();
        out.println("=== SEMUA PEMESANAN ===");
        List<Reservation> all = app.getReservations();
        if (all.isEmpty()) {
            out.println("Belum ada pemesanan.");
            return;
        }

        List<String[]> rows = all.stream()
                .map(r -> new String[]{
                        String.valueOf(r.getConfirmationNumber()),
                        r.getTypeLabel(),
                        r.summary(),
                        CurrencyFormat.rupiah(r.getTotalPrice()),
                        r.getBookedAt().format(DateFormats.DATE_TIME)})
                .toList();
        out.println(ConsoleStyle.table(
                new String[]{"No. Konfirmasi", "Jenis", "Ringkasan", "Total", "Dipesan"}, rows, 3));
        app.countByType().forEach((type, count) -> out.println("  " + type + ": " + count + " pemesanan"));
        out.println("  Total pengeluaran: " + CurrencyFormat.rupiah(app.totalSpent()));

        out.println();
        OptionalInt number = input.promptOptionalInt("Masukkan nomor konfirmasi untuk melihat detail (kosongkan untuk kembali)");
        if (number.isPresent()) {
            try {
                app.findReservation(number.getAsInt()).display(out);
            } catch (ReservationNotFoundException e) {
                out.println("! " + e.getMessage());
            }
        }
    }
}
