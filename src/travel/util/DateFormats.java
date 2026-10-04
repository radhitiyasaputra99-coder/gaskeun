package travel.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFormats {

    private static final Locale INDONESIA = new Locale("id", "ID");

    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", INDONESIA);

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", INDONESIA);

    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private DateFormats() {
    }
}
