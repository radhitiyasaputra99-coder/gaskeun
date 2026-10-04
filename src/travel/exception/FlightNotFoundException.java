package travel.exception;

/** Dilempar saat nomor penerbangan yang dipilih tidak ada pada hasil pencarian. */
public class FlightNotFoundException extends BookingException {

    private static final long serialVersionUID = 1L;

    public FlightNotFoundException(String flightNumber) {
        super("Nomor penerbangan '" + flightNumber + "' tidak ditemukan pada hasil pencarian.");
    }
}
