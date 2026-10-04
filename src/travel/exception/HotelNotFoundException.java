package travel.exception;

/** Dilempar saat ID hotel yang dipilih tidak ada pada hasil pencarian. */
public class HotelNotFoundException extends BookingException {

    private static final long serialVersionUID = 1L;

    public HotelNotFoundException(String hotelId) {
        super("ID hotel '" + hotelId + "' tidak ditemukan pada hasil pencarian.");
    }
}
