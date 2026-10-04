package travel.exception;

/** Dilempar saat jumlah penumpang melebihi kursi yang tersisa di penerbangan. */
public class InsufficientSeatsException extends BookingException {

    private static final long serialVersionUID = 1L;

    public InsufficientSeatsException(String flightNumber, int requested, int available) {
        super("Kursi tidak cukup di penerbangan " + flightNumber
                + ": diminta " + requested + ", tersedia " + available + ".");
    }
}
