package com.bookmyshow.model;

import com.bookmyshow.enums.SeatStatus;

import java.time.LocalDateTime;

/**
 * ShowSeat represents the state of one physical Seat for one specific Show.
 * This is the key distinction: the same physical Seat can be AVAILABLE
 * for Show 1 and BOOKED for Show 2. ShowSeat bridges a Seat to a Show
 * and tracks its booking/lock state per show.
 */
public class ShowSeat {
    private final String id;
    private final Seat seat;
    private double price;
    private SeatStatus status;
    private String lockedBy;      // userId who locked this seat
    private LocalDateTime lockExpiryTime;

    public ShowSeat(String id, Seat seat, double price) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ShowSeat ID cannot be null or blank");
        if (seat == null) throw new IllegalArgumentException("Seat cannot be null");
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.id = id;
        this.seat = seat;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getId() { return id; }
    public Seat getSeat() { return seat; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public LocalDateTime getLockExpiryTime() { return lockExpiryTime; }
    public void setLockExpiryTime(LocalDateTime lockExpiryTime) { this.lockExpiryTime = lockExpiryTime; }
}