package travel.data;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import travel.model.Flight;
import travel.model.Hotel;
import travel.service.TravelApp;

public final class SampleData {

    private static final int DAYS_AHEAD = 30;

    private record Route(String number, String airline, String origin, String destination,
                         LocalTime departure, int durationMinutes, long basePrice) {
    }

    private static final List<Route> ROUTES = List.of(
            new Route("GA-410", "Garuda Indonesia", "Jakarta", "Denpasar", LocalTime.of(7, 0), 110, 1_450_000),
            new Route("JT-650", "Lion Air", "Jakarta", "Denpasar", LocalTime.of(12, 15), 115, 950_000),
            new Route("SJ-262", "Sriwijaya Air", "Jakarta", "Denpasar", LocalTime.of(18, 30), 115, 1_050_000),
            new Route("GA-411", "Garuda Indonesia", "Denpasar", "Jakarta", LocalTime.of(10, 30), 120, 1_450_000),
            new Route("JT-651", "Lion Air", "Denpasar", "Jakarta", LocalTime.of(15, 0), 120, 950_000),
            new Route("QG-801", "Citilink", "Jakarta", "Yogyakarta", LocalTime.of(6, 30), 65, 780_000),
            new Route("QG-802", "Citilink", "Yogyakarta", "Jakarta", LocalTime.of(9, 0), 70, 790_000),
            new Route("GA-330", "Garuda Indonesia", "Jakarta", "Surabaya", LocalTime.of(8, 0), 95, 1_100_000),
            new Route("JT-560", "Lion Air", "Surabaya", "Jakarta", LocalTime.of(17, 45), 100, 880_000),
            new Route("ID-6310", "Batik Air", "Jakarta", "Medan", LocalTime.of(9, 20), 150, 1_350_000),
            new Route("ID-6311", "Batik Air", "Medan", "Jakarta", LocalTime.of(13, 0), 155, 1_350_000),
            new Route("GA-640", "Garuda Indonesia", "Jakarta", "Makassar", LocalTime.of(5, 45), 155, 1_750_000),
            new Route("JT-770", "Lion Air", "Makassar", "Jakarta", LocalTime.of(21, 30), 160, 1_250_000),
            new Route("QG-920", "Citilink", "Surabaya", "Denpasar", LocalTime.of(11, 0), 60, 650_000),
            new Route("QG-921", "Citilink", "Denpasar", "Surabaya", LocalTime.of(14, 0), 60, 650_000),
            new Route("JT-340", "Lion Air", "Bandung", "Denpasar", LocalTime.of(13, 30), 130, 1_100_000),
            new Route("JT-341", "Lion Air", "Denpasar", "Bandung", LocalTime.of(16, 30), 135, 1_100_000),
            new Route("QG-701", "Citilink", "Yogyakarta", "Denpasar", LocalTime.of(10, 0), 90, 850_000),
            new Route("QG-702", "Citilink", "Denpasar", "Yogyakarta", LocalTime.of(12, 30), 90, 850_000)
    );

    private SampleData() {
    }

    public static TravelApp createApp(Clock clock) {
        return new TravelApp(clock, flights(LocalDate.now(clock)), hotels());
    }

    public static List<Flight> flights(LocalDate today) {
        List<Flight> result = new ArrayList<>();
        for (int day = 0; day < DAYS_AHEAD; day++) {
            LocalDate date = today.plusDays(day);
            for (int r = 0; r < ROUTES.size(); r++) {
                Route route = ROUTES.get(r);
                double factor = 1.0 + 0.03 * ((day * 7 + r) % 6);
                long price = Math.round(route.basePrice() * factor / 1000.0) * 1000;
                int seats = 6 + (day * 5 + r * 11) % 42;
                result.add(new Flight(route.number(), route.airline(), route.origin(), route.destination(),
                        date, route.departure(), route.durationMinutes(), price, seats));
            }
        }
        return result;
    }

    public static List<Hotel> hotels() {
        return List.of(
                new Hotel("H-101", "Grand Indonesia Suites", "Jakarta", 5, 2_100_000, 20, 3),
                new Hotel("H-102", "Cikini Budget Inn", "Jakarta", 2, 450_000, 12, 2),
                new Hotel("H-103", "Sudirman Business Hotel", "Jakarta", 4, 1_250_000, 18, 3),
                new Hotel("H-201", "Kuta Beach Resort", "Denpasar", 4, 1_650_000, 15, 3),
                new Hotel("H-202", "Ubud Garden Villa", "Denpasar", 5, 2_800_000, 6, 4),
                new Hotel("H-203", "Sanur Backpacker House", "Denpasar", 2, 380_000, 10, 2),
                new Hotel("H-204", "Seminyak Boutique", "Denpasar", 4, 1_900_000, 2, 2),
                new Hotel("H-301", "Malioboro Heritage", "Yogyakarta", 4, 1_050_000, 14, 3),
                new Hotel("H-302", "Prawirotaman Homestay", "Yogyakarta", 2, 320_000, 8, 2),
                new Hotel("H-401", "Tunjungan Plaza Hotel", "Surabaya", 4, 980_000, 16, 3),
                new Hotel("H-501", "Dago Highland Hotel", "Bandung", 4, 1_150_000, 12, 3),
                new Hotel("H-502", "Braga Boutique", "Bandung", 3, 650_000, 9, 2),
                new Hotel("H-601", "Kesawan Grand", "Medan", 4, 890_000, 12, 3),
                new Hotel("H-701", "Losari Bay Hotel", "Makassar", 4, 1_020_000, 12, 3)
        );
    }
}
