package travel.ui;

/** Dilempar saat input ditutup (EOF / Ctrl+D / Ctrl+Z) agar program berhenti dengan rapi. */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("Input ditutup.");
    }
}
