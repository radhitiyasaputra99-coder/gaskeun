package travel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import travel.data.SampleData;
import travel.exception.FlightNotFoundException;
import travel.exception.HotelNotFoundException;
import travel.exception.InsufficientSeatsException;
import travel.exception.NoRoomsAvailableException;
import travel.exception.ReservationNotFoundException;
import travel.model.Flight;
import travel.model.FlightReservation;
import travel.model.FlightSearchCriteria;
import travel.model.Hotel;
import travel.model.HotelReservation;
import travel.model.HotelSearchCriteria;
import travel.model.Reservation;
import travel.service.ConfirmationGenerator;
import travel.service.TravelApp;
import travel.ui.ConsoleMenu;
import travel.util.CityDirectory;
import travel.util.Formats;

public final class TravelAppTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-04T03:00:00Z"), ZoneId.of("Asia/Jakarta"));
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 4);
    private static final LocalDate TRIP = TODAY.plusDays(3);
    private static final String PHONE = "081234567890";

    private static int passed;
    private static int failed;

    @FunctionalInterface
    private interface Body {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        System.out.println("== Entitas & utilitas ==");
        test("Format Rupiah memakai titik pemisah ribuan", () ->
                assertEquals("Rp 1.450.000", Formats.rupiah(1_450_000), "format"));
        test("CityDirectory mengenali nama, kode, dan alias (case-insensitive)", () -> {
            assertEquals(Optional.of("Denpasar"), CityDirectory.resolve("BALI"), "alias");
            assertEquals(Optional.of("Jakarta"), CityDirectory.resolve(" cgk "), "kode");
            assertEquals(Optional.of("Yogyakarta"), CityDirectory.resolve("Jogja"), "alias jogja");
            assertTrue(CityDirectory.resolve("Atlantis").isEmpty(), "kota tak dikenal");
        });
        test("Nomor konfirmasi selalu 6 digit dan unik", () -> {
            Set<Integer> taken = new HashSet<>();
            Random random = new Random(42);
            for (int i = 0; i < 2000; i++) {
                int n = ConfirmationGenerator.generate(taken, random);
                assertTrue(n >= 100_000 && n <= 999_999, "harus 6 digit: " + n);
                assertTrue(taken.add(n), "tidak boleh duplikat: " + n);
            }
        });
        test("Criteria memvalidasi dirinya sendiri", () -> {
            assertThrows(IllegalArgumentException.class, () ->
                    new FlightSearchCriteria("Jakarta", "jakarta", TRIP, 1));
            assertThrows(IllegalArgumentException.class, () ->
                    new FlightSearchCriteria("Jakarta", "Denpasar", TRIP, 0));
            assertThrows(IllegalArgumentException.class, () ->
                    new HotelSearchCriteria("Denpasar", TRIP, TRIP, 2));
            assertThrows(IllegalArgumentException.class, () ->
                    new HotelSearchCriteria("Denpasar", TRIP, TRIP.plusDays(31), 2));
            assertThrows(IllegalArgumentException.class, () ->
                    new HotelSearchCriteria("Denpasar", TRIP, TRIP.plusDays(1), 99));
        });
        test("Reservation adalah sealed class dengan tepat dua subclass", () -> {
            assertTrue(Reservation.class.isSealed(), "harus sealed");
            assertEquals(2, Reservation.class.getPermittedSubclasses().length, "jumlah subclass");
        });

        System.out.println("== Pencarian & pemesanan penerbangan ==");
        test("Pencarian memfilter rute/tanggal dan mengurutkan dari termurah", () -> {
            List<Flight> results = app().searchFlights(route(1));
            assertTrue(results.size() >= 3, "Jakarta->Denpasar minimal 3 penerbangan, dapat " + results.size());
            for (int i = 0; i < results.size(); i++) {
                Flight f = results.get(i);
                assertEquals("Jakarta", f.getOrigin(), "asal");
                assertEquals("Denpasar", f.getDestination(), "tujuan");
                assertEquals(TRIP, f.getDate(), "tanggal");
                if (i > 0) {
                    assertTrue(results.get(i - 1).getPrice() <= f.getPrice(), "harus urut harga naik");
                }
            }
        });
        test("Pencarian rute tanpa penerbangan mengembalikan list kosong", () ->
                assertTrue(app().searchFlights(new FlightSearchCriteria("Bandung", "Medan", TRIP, 1)).isEmpty(),
                        "Bandung->Medan tidak ada"));
        test("Penumpang melebihi kursi menyaring penerbangan tersebut", () -> {
            TravelApp app = app();
            List<Flight> one = app.searchFlights(route(1));
            List<Flight> nine = app.searchFlights(route(9));
            assertTrue(nine.stream().allMatch(f -> f.getSeatsAvailable() >= 9), "semua hasil punya >= 9 kursi");
            assertTrue(nine.size() <= one.size(), "syarat penumpang lebih besar tidak menambah hasil");
        });
        test("Penerbangan yang sudah berangkat hari ini tidak ditampilkan", () -> {
            List<Flight> today = app().searchFlights(new FlightSearchCriteria("Jakarta", "Denpasar", TODAY, 1));
            List<String> numbers = today.stream().map(Flight::getFlightNumber).toList();
            assertTrue(!numbers.contains("GA-410"), "GA-410 (07:00) harus disembunyikan: " + numbers);
            assertTrue(numbers.contains("JT-650") && numbers.contains("SJ-262"), "penerbangan siang/malam tetap tampil: " + numbers);
        });
        test("Pencarian menolak tanggal di masa lalu", () ->
                assertThrows(IllegalArgumentException.class, () ->
                        app().searchFlights(new FlightSearchCriteria("Jakarta", "Denpasar", TODAY.minusDays(1), 1))));
        test("bookFlight mengurangi kursi, menyimpan reservasi, dan nomor 6 digit", () -> {
            TravelApp app = app();
            Flight flight = app.searchFlights(route(2)).get(0);
            int before = flight.getSeatsAvailable();
            FlightReservation r = app.bookFlight(flight, List.of("Budi Santoso", "Ani Lestari"), PHONE);
            assertEquals(before - 2, flight.getSeatsAvailable(), "kursi berkurang 2");
            assertEquals(1, app.getReservations().size(), "tersimpan");
            assertTrue(r.getConfirmationNumber() >= 100_000 && r.getConfirmationNumber() <= 999_999, "6 digit");
            assertEquals(2L * flight.getPrice(), r.getTotalPrice(), "total = harga x penumpang");
        });
        test("Snapshot Flight menyimpan penumpang & konfirmasi tanpa mengotori inventori", () -> {
            TravelApp app = app();
            Flight inventory = app.searchFlights(route(1)).get(0);
            FlightReservation r = app.bookFlight(inventory, List.of("Budi Santoso"), PHONE);
            assertEquals(r.getConfirmationNumber(), r.getFlight().getConfirmationNumber(), "snapshot konfirmasi");
            assertEquals(1, r.getFlight().getPassengers(), "snapshot penumpang");
            assertEquals(0, inventory.getConfirmationNumber(), "inventori tidak berubah");
        });
        test("Kursi tidak cukup melempar InsufficientSeatsException", () -> {
            TravelApp app = app();
            Flight flight = app.searchFlights(route(1)).get(0);
            List<String> tooMany = Collections.nCopies(flight.getSeatsAvailable() + 1, "Budi Santoso");
            assertThrows(InsufficientSeatsException.class, () -> app.bookFlight(flight, tooMany, PHONE));
            assertEquals(0, app.getReservations().size(), "tidak ada reservasi tersimpan");
        });
        test("findFlight dengan nomor tidak ada melempar FlightNotFoundException", () ->
                assertThrows(FlightNotFoundException.class, () -> app().findFlight("XX-999", route(1))));
        test("Pembatalan penerbangan mengembalikan kursi & menghapus reservasi", () -> {
            TravelApp app = app();
            Flight flight = app.searchFlights(route(1)).get(0);
            int before = flight.getSeatsAvailable();
            FlightReservation r = app.bookFlight(flight, List.of("Budi Santoso", "Ani Lestari"), PHONE);
            String message = app.cancelReservation(r.getConfirmationNumber());
            assertTrue(message.contains("PENERBANGAN"), "pesan menyebut penerbangan: " + message);
            assertEquals(before, flight.getSeatsAvailable(), "kursi kembali");
            assertTrue(app.getReservations().isEmpty(), "reservasi terhapus");
        });
        test("Membatalkan nomor yang tidak ada melempar ReservationNotFoundException", () ->
                assertThrows(ReservationNotFoundException.class, () -> app().cancelReservation(123_456)));

        System.out.println("== Pencarian & pemesanan hotel ==");
        test("Pencarian hotel memfilter kota dan mengurutkan termurah", () -> {
            List<Hotel> results = app().searchHotels(stay(2));
            assertTrue(results.size() >= 3, "Denpasar punya >= 3 hotel untuk 2 tamu");
            assertTrue(results.stream().allMatch(h -> h.getLocation().equals("Denpasar")), "semua di Denpasar");
            for (int i = 1; i < results.size(); i++) {
                assertTrue(results.get(i - 1).getPrice() <= results.get(i).getPrice(), "urut harga");
            }
        });
        test("Jumlah tamu menentukan kebutuhan kamar dan total harga", () -> {
            Hotel kuta = app().searchHotels(stay(2)).stream()
                    .filter(h -> h.getHotelId().equals("H-201")).findFirst().orElseThrow();
            assertEquals(1, kuta.roomsNeededFor(3), "3 tamu muat 1 kamar (maks 3/kamar)");
            assertEquals(2, kuta.roomsNeededFor(4), "4 tamu butuh 2 kamar");
            assertEquals(3L * 2 * 1_650_000, kuta.stayPrice(TRIP, TRIP.plusDays(3), 4), "3 malam x 2 kamar");
        });
        test("Kamar yang habis pada tanggal tumpang tindih tidak muncul; tanggal bersebelahan tetap tersedia", () -> {
            TravelApp app = app();
            Hotel seminyak = app.findHotel("H-204", stay(2));
            app.bookHotel(seminyak, stay(2), "Budi Santoso", PHONE);
            app.bookHotel(seminyak, stay(2), "Ani Lestari", PHONE);
            assertTrue(app.searchHotels(stay(2)).stream().noneMatch(h -> h.getHotelId().equals("H-204")),
                    "H-204 harus habis");
            assertThrows(NoRoomsAvailableException.class, () -> app.bookHotel(seminyak, stay(2), "Citra", PHONE));
            HotelSearchCriteria after = new HotelSearchCriteria("Denpasar", TRIP.plusDays(2), TRIP.plusDays(4), 2);
            assertTrue(app.searchHotels(after).stream().anyMatch(h -> h.getHotelId().equals("H-204")),
                    "H-204 tersedia lagi setelah check-out");
        });
        test("Pembatalan hotel melepas kamar sehingga bisa dipesan lagi", () -> {
            TravelApp app = app();
            Hotel seminyak = app.findHotel("H-204", stay(2));
            HotelReservation first = app.bookHotel(seminyak, stay(2), "Budi Santoso", PHONE);
            app.bookHotel(seminyak, stay(2), "Ani Lestari", PHONE);
            String message = app.cancelReservation(first.getConfirmationNumber());
            assertTrue(message.contains("HOTEL"), "pesan menyebut hotel: " + message);
            assertEquals(1, app.availableRooms(seminyak, TRIP, TRIP.plusDays(2)), "1 kamar kembali");
        });
        test("findHotel dengan ID tidak ada melempar HotelNotFoundException", () ->
                assertThrows(HotelNotFoundException.class, () -> app().findHotel("H-999", stay(2))));

        System.out.println("== Polimorfisme & daftar pemesanan ==");
        test("Daftar campuran diproses polimorfik lewat referensi Reservation", () -> {
            TravelApp app = app();
            app.bookFlight(app.searchFlights(route(1)).get(0), List.of("Budi Santoso"), PHONE);
            app.bookHotel(app.findHotel("H-201", stay(2)), stay(2), "Budi Santoso", PHONE);
            List<Reservation> all = app.getReservations();
            assertEquals(2, all.size(), "dua reservasi");
            assertEquals(all.stream().mapToLong(Reservation::getTotalPrice).sum(), app.totalSpent(), "total");
            assertEquals(1L, app.countByType().get("Penerbangan"), "1 penerbangan");
            assertEquals(1L, app.countByType().get("Hotel"), "1 hotel");
            for (Reservation r : all) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                r.display(new PrintStream(buf, true, StandardCharsets.UTF_8));
                assertTrue(buf.toString(StandardCharsets.UTF_8).contains(String.valueOf(r.getConfirmationNumber())),
                        "struk memuat nomor konfirmasi");
            }
        });

        System.out.println("== Simulasi konsol end-to-end ==");
        test("Input non-numerik pada menu & jumlah penumpang di-handle lalu diminta ulang", () -> {
            String out = cli(app(), "abc", "99", "1", "jakarta", "bali", TRIP.toString(), "dua", "2", "n", "0");
            assertTrue(out.contains("Input harus berupa angka"), "pesan angka salah");
            assertTrue(out.contains("Masukkan angka antara 0 dan 6"), "pesan rentang menu");
            assertTrue(out.contains("GA-410"), "hasil pencarian tampil");
            assertTrue(out.contains("Terima kasih"), "keluar normal");
        });
        test("Pencarian tanpa hasil menampilkan 'tidak ada penerbangan tersedia'", () -> {
            String out = cli(app(), "1", "bandung", "medan", TRIP.toString(), "1", "n", "0");
            assertTrue(out.contains("tidak ada penerbangan tersedia"), "pesan kosong");
        });
        test("Kota tak dikenal dan tanggal salah format diminta ulang", () -> {
            String out = cli(app(), "1", "atlantis", "jakarta", "bali", "besok", "2020-01-01", TRIP.toString(), "1", "n", "0");
            assertTrue(out.contains("Kota tidak dikenal"), "kota");
            assertTrue(out.contains("Format tanggal salah"), "format tanggal");
            assertTrue(out.contains("Tanggal tidak boleh sebelum"), "tanggal lampau");
        });
        test("Alur pesan penerbangan penuh: ID salah, nama/kontak invalid, lalu sukses", () -> {
            TravelApp app = app();
            String out = cli(app,
                    "3", "jakarta", "denpasar", TRIP.toString(), "2",
                    "XX-999",
                    "GA-410",
                    "12345", "Budi Santoso",
                    "Ani Lestari",
                    "bukan-kontak", PHONE,
                    "y",
                    "6", "", "0");
            assertTrue(out.contains("tidak ditemukan pada hasil pencarian"), "ID salah");
            assertTrue(out.contains("Nama tidak valid"), "nama invalid");
            assertTrue(out.contains("Kontak tidak valid"), "kontak invalid");
            assertTrue(out.contains("Pemesanan berhasil"), "sukses");
            assertTrue(out.contains("E-TIKET PENERBANGAN"), "tiket tampil");
            assertTrue(out.contains("SEMUA PEMESANAN"), "daftar tampil");
            assertEquals(1, app.getReservations().size(), "tersimpan 1");
        });
        test("Alur pesan hotel lalu batalkan lewat nomor konfirmasi", () -> {
            TravelApp app = app();
            cli(app, "4", "denpasar", TRIP.toString(), TRIP.plusDays(2).toString(), "2",
                    "H-999", "H-203", "Budi Santoso", "budi@example.com", "y", "0");
            assertEquals(1, app.getReservations().size(), "hotel tersimpan");
            int number = app.getReservations().get(0).getConfirmationNumber();

            String tolak = cli(app, "5", "999999", "0");
            assertTrue(tolak.contains("tidak ditemukan"), "nomor salah ditolak");

            String out = cli(app, "5", String.valueOf(number), "y", "0");
            assertTrue(out.contains("VOUCHER HOTEL"), "voucher tampil sebelum batal");
            assertTrue(out.contains("berhasil dibatalkan"), "pesan sukses");
            assertTrue(app.getReservations().isEmpty(), "reservasi terhapus");
        });
        test("Menjawab 'n' saat konfirmasi pembatalan membiarkan reservasi tetap aktif", () -> {
            TravelApp app = app();
            Flight flight = app.searchFlights(route(1)).get(0);
            int before = flight.getSeatsAvailable();
            FlightReservation r = app.bookFlight(flight, List.of("Budi Santoso"), PHONE);
            String out = cli(app, "5", String.valueOf(r.getConfirmationNumber()), "n", "0");
            assertTrue(out.contains("Reservasi tetap aktif"), "pesan tetap aktif");
            assertEquals(1, app.getReservations().size(), "reservasi masih ada");
            assertEquals(before - 1, flight.getSeatsAvailable(), "kursi tidak dikembalikan");
        });
        test("Input ditutup di tengah jalan (EOF) tidak crash", () -> {
            String out = cli(app(), "1", "jakarta");
            assertTrue(out.contains("Input ditutup"), "pesan EOF");
        });
        test("Daftar pemesanan kosong menampilkan 'Belum ada pemesanan'", () ->
                assertTrue(cli(app(), "6", "0").contains("Belum ada pemesanan"), "pesan kosong"));

        System.out.printf("%nHasil: %d lulus, %d gagal.%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static TravelApp app() {
        return SampleData.createApp(CLOCK);
    }

    private static FlightSearchCriteria route(int passengers) {
        return new FlightSearchCriteria("Jakarta", "Denpasar", TRIP, passengers);
    }

    private static HotelSearchCriteria stay(int guests) {
        return new HotelSearchCriteria("Denpasar", TRIP, TRIP.plusDays(2), guests);
    }

    private static String cli(TravelApp app, String... inputLines) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        byte[] script = (String.join("\n", inputLines) + "\n").getBytes(StandardCharsets.UTF_8);
        new ConsoleMenu(app, new ByteArrayInputStream(script), out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void test(String name, Body body) {
        try {
            body.run();
            passed++;
            System.out.println("  [PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("  [FAIL] " + name + "\n         -> " + t);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": diharapkan <" + expected + "> tetapi <" + actual + ">");
        }
    }

    private static <T extends Throwable> void assertThrows(Class<T> type, Body body) {
        try {
            body.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("diharapkan " + type.getSimpleName() + " tetapi " + t, t);
        }
        throw new AssertionError("diharapkan " + type.getSimpleName() + " tetapi tidak ada exception");
    }
}
