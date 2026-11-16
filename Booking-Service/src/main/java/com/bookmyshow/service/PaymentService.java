package com.bookmyshow.service;

import com.bookmyshow.enums.PaymentStatus;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Payment;

import java.util.UUID;

/**
 * PaymentService handles payment gateway interactions.
 * It is responsible for processing payments and returning results.
 * This is composed by BookingManager for booking-related payment operations.
 *
 * This follows the same pattern as other domain services (UserService, MovieService, etc.)
 * and keeps payment-specific logic separated from booking lifecycle management.
 */


public class PaymentService {

    private static final PaymentService INSTANCE = new PaymentService();

    private PaymentService() {
    }

    public static PaymentService getInstance() {
        return INSTANCE;
    }

    /**
     * Processes payment for a booking.
     */
    public Payment processPayment(Booking booking) {

        String paymentId = UUID.randomUUID().toString();

        Payment payment =
                new Payment(paymentId, booking.getAmount());

        // Simulate payment gateway response
        boolean paymentSuccess = true;

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return payment;
    }
}