package travel.model;

import java.time.LocalDate;
import java.util.Objects;

public record FlightSearchCriteria(String origin, String destination, LocalDate date, int passengers) {

    public static final int MAX_PASSENGERS = 9;

    public FlightSearchCriteria {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(date, "date");
        if (origin.equalsIgnoreCase(destination)) {
            throw new IllegalArgumentException("Kota asal dan kota tujuan tidak boleh sama.");
        }
        if (passengers < 1 || passengers > MAX_PASSENGERS) {
            throw new IllegalArgumentException("Jumlah penumpang harus 1 sampai " + MAX_PASSENGERS + ".");
        }
    }
}
