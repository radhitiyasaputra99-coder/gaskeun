package travel.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Utilitas format mata uang Rupiah (contoh: Rp 1.450.000).
 * Dibuat final karena hanya berisi metode static dan tidak boleh diwarisi.
 */
public final class CurrencyFormat {

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols();

    static {
        SYMBOLS.setGroupingSeparator('.');
        SYMBOLS.setDecimalSeparator(',');
    }

    private CurrencyFormat() {
        // utility class: tidak perlu di-instansiasi
    }

    /** Memformat nilai rupiah, misalnya 1450000 menjadi "Rp 1.450.000". */
    public static String rupiah(long amount) {
        // DecimalFormat tidak thread-safe, jadi dibuat baru setiap pemanggilan.
        return "Rp " + new DecimalFormat("#,##0", SYMBOLS).format(amount);
    }
}
