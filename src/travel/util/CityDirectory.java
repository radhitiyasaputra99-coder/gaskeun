package travel.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Daftar kota yang didukung beserta kode bandara dan alias populer.
 * Dipakai untuk memvalidasi input kota, jadi "bali", "DPS", dan "Denpasar"
 * semuanya dikenali sebagai kota "Denpasar".
 */
public final class CityDirectory {

    /** kota kanonik -> kode bandara (urutan sesuai registrasi). */
    private static final Map<String, String> CODES = new LinkedHashMap<>();

    /** teks huruf kecil (nama/kode/alias) -> kota kanonik. */
    private static final Map<String, String> LOOKUP = new HashMap<>();

    static {
        register("Jakarta", "CGK", "jkt");
        register("Surabaya", "SUB", "sby");
        register("Denpasar", "DPS", "bali");
        register("Yogyakarta", "YIA", "jogja", "yogya", "jogjakarta");
        register("Bandung", "BDO");
        register("Medan", "KNO");
        register("Makassar", "UPG");
    }

    private CityDirectory() {
    }

    private static void register(String city, String code, String... aliases) {
        CODES.put(city, code);
        LOOKUP.put(city.toLowerCase(Locale.ROOT), city);
        LOOKUP.put(code.toLowerCase(Locale.ROOT), city);
        for (String alias : aliases) {
            LOOKUP.put(alias.toLowerCase(Locale.ROOT), city);
        }
    }

    /** Mengubah input pengguna (nama, kode, atau alias) menjadi nama kota kanonik. */
    public static Optional<String> resolve(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP.get(input.trim().toLowerCase(Locale.ROOT)));
    }

    /** Kode bandara untuk sebuah kota; jika tidak dikenal, nama kota dikembalikan apa adanya. */
    public static String codeOf(String city) {
        return CODES.getOrDefault(city, city);
    }

    public static List<String> cities() {
        return List.copyOf(CODES.keySet());
    }

    /** Contoh: "Jakarta (CGK), Surabaya (SUB), ...". */
    public static String describeAll() {
        return CODES.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
    }
}
