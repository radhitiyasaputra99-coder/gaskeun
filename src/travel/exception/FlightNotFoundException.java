package travel.exception;

public class FlightNotFoundException extends BookingException {

    private static final long serialVersionUID = 1L;

    public FlightNotFoundException(String flightNumber) {
        super("Nomor penerbangan '" + flightNumber + "' tidak ditemukan pada hasil pencarian.");
    }
}
