package travel.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Kumpulan formatter tanggal/waktu agar tampilan konsisten di seluruh aplikasi. */
public final class DateFormats {

    private static final Locale INDONESIA = new Locale("id", "ID");

    /** Format input pengguna: yyyy-MM-dd. */
    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Contoh: "Rab, 07 Okt 2026". */
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", INDONESIA);

    /** Contoh: "07 Okt 2026 14:30". */
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", INDONESIA);

    /** Contoh: "07:00". */
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private DateFormats() {
    }
}
