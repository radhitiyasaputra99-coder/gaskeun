package travel.model;

import java.time.LocalDate;
import java.util.Objects;

public record HotelSearchCriteria(String location, LocalDate checkIn, LocalDate checkOut, int guests) {

    public static final int MAX_GUESTS = 8;
    public static final int MAX_NIGHTS = 30;

    public HotelSearchCriteria {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(checkIn, "checkIn");
        Objects.requireNonNull(checkOut, "checkOut");
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Tanggal check-out harus setelah tanggal check-in.");
        }
        if (Hotel.nightsBetween(checkIn, checkOut) > MAX_NIGHTS) {
            throw new IllegalArgumentException("Lama menginap maksimal " + MAX_NIGHTS + " malam.");
        }
        if (guests < 1 || guests > MAX_GUESTS) {
            throw new IllegalArgumentException("Jumlah tamu harus 1 sampai " + MAX_GUESTS + ".");
        }
    }

    public int nights() {
        return Hotel.nightsBetween(checkIn, checkOut);
    }
}
