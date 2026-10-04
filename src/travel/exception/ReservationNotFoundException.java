package travel.exception;

/** Dilempar saat nomor konfirmasi yang akan dibatalkan/dilihat tidak ada di daftar reservasi. */
public class ReservationNotFoundException extends BookingException {

    private static final long serialVersionUID = 1L;

    public ReservationNotFoundException(int confirmationNumber) {
        super("Reservasi dengan nomor konfirmasi " + confirmationNumber + " tidak ditemukan.");
    }
}
