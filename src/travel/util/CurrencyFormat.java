package travel.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class CurrencyFormat {

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols();

    static {
        SYMBOLS.setGroupingSeparator('.');
        SYMBOLS.setDecimalSeparator(',');
    }

    private CurrencyFormat() {
    }

    public static String rupiah(long amount) {
        return "Rp " + new DecimalFormat("#,##0", SYMBOLS).format(amount);
    }
}
