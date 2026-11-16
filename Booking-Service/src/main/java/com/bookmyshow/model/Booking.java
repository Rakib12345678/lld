package com.bookmyshow.model;

import com.bookmyshow.enums.BookingStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Booking represents a booking made by a User for a Show.
 * It owns a Payment (composition) — the Payment lifecycle is tied to the Booking.
 * Payment does NOT store a bookingId because Booking already knows its own ID.
 */
public class Booking {
    private final String id;
    private final User user;
    private final Show show;
    private final List<ShowSeat> showSeats;
    private double amount;
    private BookingStatus status;
    private Payment payment; // null until payment is made

    public Booking(String id, User user, Show show, List<ShowSeat> showSeats, double amount) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Booking ID cannot be null or blank");
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (show == null) throw new IllegalArgumentException("Show cannot be null");
        if (showSeats == null || showSeats.isEmpty()) throw new IllegalArgumentException("ShowSeats cannot be null or empty");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.id = id;
        this.user = user;
        this.show = show;
        this.showSeats = new ArrayList<>(showSeats);
        this.amount = amount;
        this.status = BookingStatus.CREATED;
    }

    public String getId() { return id; }
    public User getUser() { return user; }
    public Show getShow() { return show; }
    public List<ShowSeat> getShowSeats() { return Collections.unmodifiableList(showSeats); }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}