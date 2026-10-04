package travel.model;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;

import travel.util.ConsoleStyle;

/**
 * Kelas dasar semua reservasi. Dideklarasikan {@code sealed}: hanya FlightReservation dan
 * HotelReservation yang boleh mewarisinya, sehingga hierarki tertutup dan aman diperiksa
 * dengan pattern matching.
 *
 * <p>Method abstract ({@link #display}, {@link #cancel}, dll.) di-override oleh subclass,
 * jadi kode pemanggil cukup memegang referensi {@code Reservation} (polimorfisme).
 */
public abstract sealed class Reservation permits FlightReservation, HotelReservation {

    private int confirmationNumber;
    private final String customerName;
    private final String contact;
    private final LocalDateTime bookedAt;

    protected Reservation(int confirmationNumber, String customerName, String contact, LocalDateTime bookedAt) {
        this.confirmationNumber = confirmationNumber;
        this.customerName = customerName;
        this.contact = contact;
        this.bookedAt = bookedAt;
    }

    /** Label jenis reservasi, mis. "Penerbangan" atau "Hotel". */
    public abstract String getTypeLabel();

    /** Total biaya reservasi dalam Rupiah. */
    public abstract long getTotalPrice();

    /** Ringkasan satu baris untuk daftar pemesanan. */
    public abstract String summary();

    /** Menampilkan tiket/struk lengkap. */
    public abstract void display(PrintStream out);

    /**
     * Melepas sumber daya yang dipegang reservasi (kursi/kamar) dan mengembalikan
     * catatan singkat tentang apa yang dilepas.
     */
    public abstract String cancel();

    /** Menampilkan struk ke System.out. */
    public void display() {
        display(System.out);
    }

    // ---------- helper untuk subclass ----------

    protected static String field(String label, String value) {
        return String.format("%-15s: %s", label, value);
    }

    protected static void printBox(PrintStream out, String title, List<String> lines) {
        out.println(ConsoleStyle.box(title, lines));
    }

    // ---------- getter / setter ----------

    public int getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(int confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getContact() {
        return contact;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }
}
