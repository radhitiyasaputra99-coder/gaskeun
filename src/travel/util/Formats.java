package travel.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formats {

    private static final Locale INDONESIA = new Locale("id", "ID");

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols();

    static {
        SYMBOLS.setGroupingSeparator('.');
        SYMBOLS.setDecimalSeparator(',');
    }

    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", INDONESIA);

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", INDONESIA);

    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Formats() {
    }

    public static String rupiah(long amount) {
        return "Rp " + new DecimalFormat("#,##0", SYMBOLS).format(amount);
    }
}
