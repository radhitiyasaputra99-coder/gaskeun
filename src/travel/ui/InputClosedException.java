package travel.ui;

public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("Input ditutup.");
    }
}
