package travel.exception;

/**
 * Induk semua kesalahan bisnis pada proses pemesanan.
 * Dibuat checked exception supaya pemanggil (UI) wajib menanganinya.
 */
public class BookingException extends Exception {

    private static final long serialVersionUID = 1L;

    public BookingException(String message) {
        super(message);
    }
}
