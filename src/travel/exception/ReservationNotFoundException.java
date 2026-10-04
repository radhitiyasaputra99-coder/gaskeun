package travel.exception;

public class ReservationNotFoundException extends BookingException {

    private static final long serialVersionUID = 1L;

    public ReservationNotFoundException(int confirmationNumber) {
        super("Reservasi dengan nomor konfirmasi " + confirmationNumber + " tidak ditemukan.");
    }
}
