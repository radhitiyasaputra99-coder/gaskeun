package travel.model;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;

import travel.util.ConsoleStyle;

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

    public abstract String getTypeLabel();

    public abstract long getTotalPrice();

    public abstract String summary();

    public abstract void display(PrintStream out);

    public abstract String cancel();

    public void display() {
        display(System.out);
    }

    protected static String field(String label, String value) {
        return String.format("%-15s: %s", label, value);
    }

    protected static void printBox(PrintStream out, String title, List<String> lines) {
        out.println(ConsoleStyle.box(title, lines));
    }

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
