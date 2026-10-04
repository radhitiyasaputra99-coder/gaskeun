package travel.model;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import travel.util.CityDirectory;
import travel.util.CurrencyFormat;
import travel.util.DateFormats;

public final class FlightReservation extends Reservation {

    private final Flight inventoryFlight;

    private final Flight flight;

    private final List<String> passengerNames;

    public FlightReservation(int confirmationNumber, String contact, LocalDateTime bookedAt,
                             Flight inventoryFlight, Flight flight, List<String> passengerNames) {
        super(confirmationNumber, passengerNames.get(0), contact, bookedAt);
        this.inventoryFlight = inventoryFlight;
        this.flight = flight;
        this.passengerNames = List.copyOf(passengerNames);
    }

    public Flight getFlight() {
        return flight;
    }

    public List<String> getPassengerNames() {
        return passengerNames;
    }

    @Override
    public String getTypeLabel() {
        return "Penerbangan";
    }

    @Override
    public long getTotalPrice() {
        return flight.getTotalPrice();
    }

    @Override
    public String summary() {
        return String.format("%s %s -> %s, %s, %d penumpang",
                flight.getFlightNumber(), flight.getOrigin(), flight.getDestination(),
                flight.getDate().format(DateFormats.ISO), flight.getPassengers());
    }

    @Override
    public String cancel() {
        inventoryFlight.releaseSeats(flight.getPassengers());
        return flight.getPassengers() + " kursi dikembalikan ke penerbangan " + flight.getFlightNumber() + ".";
    }

    @Override
    public void display(PrintStream out) {
        List<String> lines = new ArrayList<>();
        lines.add(field("No. Konfirmasi", String.valueOf(getConfirmationNumber())));
        lines.add(field("Penerbangan", flight.getFlightNumber() + " - " + flight.getAirline()));
        lines.add(field("Rute", flight.getOrigin() + " (" + CityDirectory.codeOf(flight.getOrigin()) + ") -> "
                + flight.getDestination() + " (" + CityDirectory.codeOf(flight.getDestination()) + ")"));
        lines.add(field("Berangkat", flight.getDate().format(DateFormats.DATE) + " "
                + flight.getDepartureTime().format(DateFormats.TIME)));
        lines.add(field("Tiba", flight.getArrivalLabel()));
        lines.add(field("Penumpang", flight.getPassengers() + " orang"));
        for (int i = 0; i < passengerNames.size(); i++) {
            lines.add("  " + (i + 1) + ". " + passengerNames.get(i));
        }
        lines.add(field("Kontak", getContact()));
        lines.add(field("Harga/orang", CurrencyFormat.rupiah(flight.getPrice())));
        lines.add(field("Total Bayar", CurrencyFormat.rupiah(getTotalPrice())));
        lines.add(field("Dipesan pada", getBookedAt().format(DateFormats.DATE_TIME)));
        lines.add(field("Status", "TERKONFIRMASI"));
        printBox(out, "E-TIKET PENERBANGAN", lines);
    }
}
