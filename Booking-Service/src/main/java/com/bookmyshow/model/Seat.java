package com.bookmyshow.model;

import com.bookmyshow.enums.SeatType;

/**
 * Seat represents a physical seat in a Screen.
 * It does NOT store booking status because the same physical seat
 * can be AVAILABLE for one Show and BOOKED for another.
 * The per-show status is tracked by ShowSeat.
 */
public class Seat {
    private final String id;
    private final String label;
    private final SeatType seatType;

    public Seat(String id, String label, SeatType seatType) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Seat ID cannot be null or blank");
        if (label == null || label.isBlank()) throw new IllegalArgumentException("Seat label cannot be null or blank");
        if (seatType == null) throw new IllegalArgumentException("Seat type cannot be null");
        this.id = id;
        this.label = label;
        this.seatType = seatType;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public SeatType getSeatType() { return seatType; }
}