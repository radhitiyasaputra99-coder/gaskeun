package travel.exception;

public class NoRoomsAvailableException extends BookingException {

    private static final long serialVersionUID = 1L;

    public NoRoomsAvailableException(String hotelName) {
        super("Kamar di " + hotelName + " sudah penuh pada tanggal yang dipilih.");
    }
}
