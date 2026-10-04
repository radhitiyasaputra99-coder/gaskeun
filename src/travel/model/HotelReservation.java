package travel.model;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import travel.util.Formats;

public final class HotelReservation extends Reservation {

    private final Hotel hotel;

    public HotelReservation(int confirmationNumber, String guestName, String contact,
                            LocalDateTime bookedAt, Hotel hotel) {
        super(confirmationNumber, guestName, contact, bookedAt);
        this.hotel = hotel;
    }

    public Hotel getHotel() {
        return hotel;
    }

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
                hotel.getCheckIn().format(Formats.ISO), hotel.getCheckOut().format(Formats.ISO),
                hotel.getGuests());
    }

    @Override
    public String cancel() {
        return getRooms() + " kamar dilepas kembali ke " + hotel.getName() + ".";
    }

    @Override
    public void display(PrintStream out) {
        List<String> lines = new ArrayList<>();
        lines.add(field("No. Konfirmasi", String.valueOf(getConfirmationNumber())));
        lines.add(field("Hotel", hotel.getName() + " (" + hotel.getStarsLabel() + ")"));
        lines.add(field("ID Hotel", hotel.getHotelId()));
        lines.add(field("Lokasi", hotel.getLocation()));
        lines.add(field("Check-in", hotel.getCheckIn().format(Formats.DATE)));
        lines.add(field("Check-out", hotel.getCheckOut().format(Formats.DATE)));
        lines.add(field("Lama menginap", hotel.getNights() + " malam"));
        lines.add(field("Tamu / Kamar", hotel.getGuests() + " tamu / " + getRooms() + " kamar"));
        lines.add(field("Nama tamu", getCustomerName()));
        lines.add(field("Kontak", getContact()));
        lines.add(field("Harga/malam", Formats.rupiah(hotel.getPricePerNight())));
        lines.add(field("Total Bayar", Formats.rupiah(getTotalPrice())));
        lines.add(field("Dipesan pada", getBookedAt().format(Formats.DATE_TIME)));
        lines.add(field("Status", "TERKONFIRMASI"));
        printBox(out, "VOUCHER HOTEL", lines);
    }
}
