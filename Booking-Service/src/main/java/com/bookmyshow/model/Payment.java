package com.bookmyshow.model;

import com.bookmyshow.enums.PaymentStatus;

/**
 * Payment represents a payment transaction.
 * It is owned by Booking (composition) — Payment does NOT store a bookingId
 * because the Booking that owns it already knows its own ID.
 * This avoids redundant data and makes the relationship clearer.
 */
public class Payment {
    private final String id;
    private final double amount;
    private PaymentStatus status;

    public Payment(String id, double amount) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Payment ID cannot be null or blank");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.id = id;
        this.amount = amount;
        this.status = PaymentStatus.INITIATED;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
}