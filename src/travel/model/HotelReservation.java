package travel.model;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import travel.util.CurrencyFormat;
import travel.util.DateFormats;

/** Reservasi hotel. Final: tidak boleh diwarisi lagi. */
public final class HotelReservation extends Reservation {

    /** Snapshot hotel saat dipesan (berisi tanggal menginap, jumlah tamu, nomor konfirmasi). */
    private final Hotel hotel;

    public HotelReservation(int confirmationNumber, String guestName, String contact,
                            LocalDateTime bookedAt, Hotel hotel) {
        super(confirmationNumber, guestName, contact, bookedAt);
        this.hotel = hotel;
    }

    public Hotel getHotel() {
        return hotel;
    }

    /** Jumlah kamar yang dipegang reservasi ini. */
    public int getRooms() {
        return hotel.roomsNeededFor(hotel.getGuests());
    }

    @Override
    public String getTypeLabel() {
        return "Hotel";
    }

    @Override
    public long getTotalPrice() {
        return hotel.getTotalPrice();
    }

    @Override
    public String summary() {
        return String.format("%s (%s), %s s/d %s, %d tamu",
                hotel.getName(), hotel.getLocation(),
                hotel.getCheckIn().format(DateFormats.ISO), hotel.getCheckOut().format(DateFormats.ISO),
                hotel.getGuests());
    }

    @Override
    public String cancel() {
        // Ketersediaan kamar dihitung dari daftar reservasi aktif, jadi menghapus reservasi
        // sudah otomatis melepas kamar. Di sini cukup memberi catatan.
        return getRooms() + " kamar dilepas kembali ke " + hotel.getName() + ".";
    }

    @Override
    public void display(PrintStream out) {
        List<String> lines = new ArrayList<>();
        lines.add(field("No. Konfirmasi", String.valueOf(getConfirmationNumber())));
        lines.add(field("Hotel", hotel.getName() + " (" + hotel.getStarsLabel() + ")"));
        lines.add(field("ID Hotel", hotel.getHotelId()));
        lines.add(field("Lokasi", hotel.getLocation()));
        lines.add(field("Check-in", hotel.getCheckIn().format(DateFormats.DATE)));
        lines.add(field("Check-out", hotel.getCheckOut().format(DateFormats.DATE)));
        lines.add(field("Lama menginap", hotel.getNights() + " malam"));
        lines.add(field("Tamu / Kamar", hotel.getGuests() + " tamu / " + getRooms() + " kamar"));
        lines.add(field("Nama tamu", getCustomerName()));
        lines.add(field("Kontak", getContact()));
        lines.add(field("Harga/malam", CurrencyFormat.rupiah(hotel.getPricePerNight())));
        lines.add(field("Total Bayar", CurrencyFormat.rupiah(getTotalPrice())));
        lines.add(field("Dipesan pada", getBookedAt().format(DateFormats.DATE_TIME)));
        lines.add(field("Status", "TERKONFIRMASI"));
        printBox(out, "VOUCHER HOTEL", lines);
    }
}
