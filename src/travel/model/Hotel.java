package travel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import travel.util.Formats;

public class Hotel implements Bookable {

    private final String hotelId;
    private final String name;
    private final String location;
    private final int stars;
    private final int totalRooms;
    private final int maxGuestsPerRoom;

    private long pricePerNight;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private int confirmationNumber;

    public Hotel(String hotelId, String name, String location, int stars,
                 long pricePerNight, int totalRooms, int maxGuestsPerRoom) {
        this.hotelId = hotelId;
        this.name = name;
        this.location = location;
        this.stars = stars;
        this.totalRooms = totalRooms;
        this.maxGuestsPerRoom = maxGuestsPerRoom;
        this.pricePerNight = requireNonNegative(pricePerNight);
    }

    public int roomsNeededFor(int guestCount) {
        return (guestCount + maxGuestsPerRoom - 1) / maxGuestsPerRoom;
    }

    public static int nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public long stayPrice(LocalDate checkIn, LocalDate checkOut, int guestCount) {
        return pricePerNight * nightsBetween(checkIn, checkOut) * roomsNeededFor(guestCount);
    }

    public Hotel bookedCopy(LocalDate checkIn, LocalDate checkOut, int guests, int confirmationNumber) {
        Hotel copy = new Hotel(hotelId, name, location, stars, pricePerNight, totalRooms, maxGuestsPerRoom);
        copy.setCheckIn(checkIn);
        copy.setCheckOut(checkOut);
        copy.setGuests(guests);
        copy.setConfirmationNumber(confirmationNumber);
        return copy;
    }

    public int getNights() {
        return (checkIn == null || checkOut == null) ? 0 : nightsBetween(checkIn, checkOut);
    }

    public long getTotalPrice() {
        return (checkIn == null || checkOut == null) ? 0 : stayPrice(checkIn, checkOut, guests);
    }

    public String getStarsLabel() {
        return "*".repeat(stars);
    }

    @Override
    public String getId() {
        return hotelId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getPrice() {
        return pricePerNight;
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getLocation() {
        return location;
    }

    public int getStars() {
        return stars;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public int getMaxGuestsPerRoom() {
        return maxGuestsPerRoom;
    }

    public long getPricePerNight() {
        return pricePerNight;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getGuests() {
        return guests;
    }

    public int getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setPricePerNight(long pricePerNight) {
        this.pricePerNight = requireNonNegative(pricePerNight);
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public void setGuests(int guests) {
        if (guests < 0) {
            throw new IllegalArgumentException("Jumlah tamu tidak boleh negatif.");
        }
        this.guests = guests;
    }

    public void setConfirmationNumber(int confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    private static long requireNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }
        return value;
    }

    @Override
    public String toString() {
        String stay = checkIn == null ? ""
                : " | " + checkIn.format(Formats.ISO) + " s/d " + checkOut.format(Formats.ISO);
        return String.format("%s %s (%s) | %s | %s/malam%s",
                hotelId, name, getStarsLabel(), location, Formats.rupiah(pricePerNight), stay);
    }
}
