package travel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import travel.exception.InsufficientSeatsException;
import travel.util.CurrencyFormat;
import travel.util.DateFormats;

public class Flight implements Bookable {

    private final String flightNumber;
    private final String airline;
    private final String origin;
    private final String destination;
    private final LocalDate date;
    private final LocalTime departureTime;
    private final int durationMinutes;

    private long price;
    private int seatsAvailable;
    private int passengers;
    private int confirmationNumber;

    public Flight(String flightNumber, String airline, String origin, String destination,
                  LocalDate date, LocalTime departureTime, int durationMinutes,
                  long price, int seatsAvailable) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.departureTime = departureTime;
        this.durationMinutes = durationMinutes;
        this.price = requireNonNegative(price, "Harga");
        this.seatsAvailable = (int) requireNonNegative(seatsAvailable, "Kursi tersedia");
    }

    public boolean hasSeats(int count) {
        return seatsAvailable >= count;
    }

    public void reserveSeats(int count) throws InsufficientSeatsException {
        if (!hasSeats(count)) {
            throw new InsufficientSeatsException(flightNumber, count, seatsAvailable);
        }
        seatsAvailable -= count;
    }

    public void releaseSeats(int count) {
        seatsAvailable += count;
    }

    public Flight bookedCopy(int passengers, int confirmationNumber) {
        Flight copy = new Flight(flightNumber, airline, origin, destination, date,
                departureTime, durationMinutes, price, 0);
        copy.setPassengers(passengers);
        copy.setConfirmationNumber(confirmationNumber);
        return copy;
    }

    public long getTotalPrice() {
        return price * passengers;
    }

    public LocalDateTime getDepartureDateTime() {
        return LocalDateTime.of(date, departureTime);
    }

    public LocalDateTime getArrivalDateTime() {
        return getDepartureDateTime().plusMinutes(durationMinutes);
    }

    public String getArrivalLabel() {
        LocalDateTime arrival = getArrivalDateTime();
        String time = arrival.toLocalTime().format(DateFormats.TIME);
        long dayShift = ChronoUnit.DAYS.between(date, arrival.toLocalDate());
        return dayShift > 0 ? time + " (+" + dayShift + ")" : time;
    }

    public String getDurationLabel() {
        return (durationMinutes / 60) + "j " + (durationMinutes % 60) + "m";
    }

    @Override
    public String getId() {
        return flightNumber;
    }

    @Override
    public String getName() {
        return airline;
    }

    @Override
    public long getPrice() {
        return price;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public int getPassengers() {
        return passengers;
    }

    public int getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setPrice(long price) {
        this.price = requireNonNegative(price, "Harga");
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = (int) requireNonNegative(seatsAvailable, "Kursi tersedia");
    }

    public void setPassengers(int passengers) {
        if (passengers < 0) {
            throw new IllegalArgumentException("Jumlah penumpang tidak boleh negatif.");
        }
        this.passengers = passengers;
    }

    public void setConfirmationNumber(int confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    private static long requireNonNegative(long value, String label) {
        if (value < 0) {
            throw new IllegalArgumentException(label + " tidak boleh negatif.");
        }
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s %s | %s -> %s | %s %s-%s | %s | sisa %d kursi",
                flightNumber, airline, origin, destination,
                date.format(DateFormats.ISO), departureTime.format(DateFormats.TIME), getArrivalLabel(),
                CurrencyFormat.rupiah(price), seatsAvailable);
    }
}
