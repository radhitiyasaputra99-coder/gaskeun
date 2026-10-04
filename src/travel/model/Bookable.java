package travel.model;

import travel.util.CurrencyFormat;

/**
 * Kontrak untuk semua layanan perjalanan yang bisa dipesan (Flight dan Hotel).
 * Dengan interface ini kode generik bisa memperlakukan keduanya secara polimorfik
 * tanpa tahu tipe pastinya saat kompilasi.
 */
public interface Bookable {

    /** Pengenal unik di katalog: nomor penerbangan atau ID hotel. */
    String getId();

    /** Nama layanan: maskapai atau nama hotel. */
    String getName();

    /** Harga dasar dalam Rupiah (per orang untuk penerbangan, per malam untuk hotel). */
    long getPrice();

    /** Harga dasar dalam format Rupiah, mis. "Rp 1.450.000". */
    default String priceLabel() {
        return CurrencyFormat.rupiah(getPrice());
    }
}
