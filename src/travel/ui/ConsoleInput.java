package travel.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.regex.Pattern;

import travel.util.CityDirectory;
import travel.util.DateFormats;

public class ConsoleInput implements AutoCloseable {

    private static final Pattern NAME = Pattern.compile("^\\p{L}[\\p{L} .'-]{1,59}$");
    private static final Pattern PHONE = Pattern.compile("^(\\+62|62|0)8\\d{8,12}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleInput(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in, StandardCharsets.UTF_8);
        this.out = out;
    }

    public String readLine(String label) {
        out.print(label + ": ");
        out.flush();
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new InputClosedException();
        }
    }

    public int promptInt(String label, int min, int max) {
        while (true) {
            String raw = readLine(label);
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
                out.printf("! Masukkan angka antara %d dan %d.%n", min, max);
            } catch (NumberFormatException e) {
                out.println("! Input harus berupa angka (contoh: " + min + ").");
            }
        }
    }

    public OptionalInt promptOptionalInt(String label) {
        while (true) {
            String raw = readLine(label);
            if (raw.isEmpty()) {
                return OptionalInt.empty();
            }
            try {
                return OptionalInt.of(Integer.parseInt(raw));
            } catch (NumberFormatException e) {
                out.println("! Input harus berupa angka.");
            }
        }
    }

    public String promptCity(String label) {
        while (true) {
            Optional<String> city = CityDirectory.resolve(readLine(label));
            if (city.isPresent()) {
                return city.get();
            }
            out.println("! Kota tidak dikenal. Pilihan: " + CityDirectory.describeAll());
        }
    }

    public LocalDate promptDate(String label, LocalDate minDate) {
        while (true) {
            String raw = readLine(label);
            try {
                LocalDate date = LocalDate.parse(raw);
                if (!date.isBefore(minDate)) {
                    return date;
                }
                out.println("! Tanggal tidak boleh sebelum " + minDate.format(DateFormats.ISO) + ".");
            } catch (DateTimeParseException e) {
                out.println("! Format tanggal salah. Gunakan yyyy-MM-dd (contoh: "
                        + minDate.plusDays(7).format(DateFormats.ISO) + ").");
            }
        }
    }

    public String promptName(String label) {
        while (true) {
            String raw = readLine(label);
            if (NAME.matcher(raw).matches()) {
                return raw;
            }
            out.println("! Nama tidak valid (2-60 karakter, hanya huruf, spasi, titik, apostrof, atau strip).");
        }
    }

    public String promptContact(String label) {
        while (true) {
            String raw = readLine(label);
            if (PHONE.matcher(raw).matches() || EMAIL.matcher(raw).matches()) {
                return raw;
            }
            out.println("! Kontak tidak valid. Gunakan nomor HP (mis. 081234567890) atau email.");
        }
    }

    public boolean confirm(String label) {
        while (true) {
            String answer = readLine(label + " (y/n)").toLowerCase(Locale.ROOT);
            if (answer.equals("y") || answer.equals("ya")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("t") || answer.equals("tidak")) {
                return false;
            }
            out.println("! Jawab dengan y atau n.");
        }
    }

    @Override
    public void close() {
        scanner.close();
    }
}
