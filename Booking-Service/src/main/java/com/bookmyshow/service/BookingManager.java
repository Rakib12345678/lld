package com.bookmyshow.service;

import com.bookmyshow.enums.BookingStatus;
import com.bookmyshow.enums.PaymentStatus;
import com.bookmyshow.model.*;
import com.bookmyshow.strategy.PricingStrategy;
import com.bookmyshow.strategy.PricingStrategyFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * BookingManager handles all booking lifecycle operations:
 * - ShowSeat creation with pricing strategy
 * - Booking creation (orchestrates SeatLockManager for atomic check-and-lock)
 * - Payment processing with idempotency
 * - Booking cancellation
 *
 * This is an internal component of BookingService, not a separate service.
 * It encapsulates all booking business logic.
 *
 * RELATIONSHIP: Booking owns Payment (composition).
 * Payment does NOT store a bookingId — Booking already knows its own ID.
 * This avoids redundant data and makes the relationship clearer.
 */


public class BookingManager {

    private final Map<String, Booking> bookings;
    private final SeatLockManager seatLockManager;
    private final PaymentService paymentService;

    private static final BookingManager INSTANCE = new BookingManager();

    private BookingManager() {
        this.bookings = new HashMap<>();
        this.seatLockManager = SeatLockManager.getInstance();
        this.paymentService = PaymentService.getInstance();
    }

    public static BookingManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates ShowSeat entries for every seat in the show's screen.
     */
    public void createShowSeats(
            Show show) {
        PricingStrategy strategy =
                PricingStrategyFactory.getStrategy(show);

        for (Seat seat : show.getScreen().getSeats()) {

            double price = strategy.calculatePrice(seat);

            String showSeatId =
                    show.getId() + "_" + seat.getId();

            ShowSeat showSeat =
                    new ShowSeat(showSeatId, seat, price);

            show.addShowSeat(showSeat);
        }
    }

    /**
     * Creates a booking with atomic check-and-lock.
     */
    public Booking createBooking(
            User user,
            Show show,
            List<String> seatIds) {

        ReentrantLock showLock =
                seatLockManager.getShowLock(show.getId());

        showLock.lock();

        try {

            List<ShowSeat> requestedSeats =
                    seatLockManager.resolveShowSeats(
                            show,
                            seatIds
                    );

            // Check all seats before locking
            seatLockManager.checkAllAvailable(
                    requestedSeats
            );

            // Sort to prevent deadlock
            List<String> sortedSeatIds =
                    new ArrayList<>(seatIds);

            Collections.sort(sortedSeatIds);

            seatLockManager.acquireSeatLocks(
                    show.getId(),
                    sortedSeatIds
            );

            try {

                // Re-check after acquiring locks
                seatLockManager.checkAllAvailable(
                        requestedSeats
                );

                // Lock all seats
                seatLockManager.lockSeats(
                        requestedSeats,
                        user.getId()
                );

                double totalAmount =
                        requestedSeats.stream()
                                .mapToDouble(ShowSeat::getPrice)
                                .sum();

                String bookingId =
                        UUID.randomUUID().toString();

                Booking booking =
                        new Booking(
                                bookingId,
                                user,
                                show,
                                requestedSeats,
                                totalAmount
                        );

                booking.setStatus(
                        BookingStatus.PAYMENT_PENDING
                );

                bookings.put(
                        bookingId,
                        booking
                );

                return booking;

            } finally {

                seatLockManager.releaseSeatLocks(
                        show.getId(),
                        sortedSeatIds
                );
            }

        } finally {

            showLock.unlock();
        }
    }

    /**
     * Processes payment for a booking.
     */
    public Payment makePayment(String bookingId) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Booking not found: " + bookingId
            );
        }

        // Idempotency check
        if (booking.getPayment() != null
                && booking.getPayment().getStatus()
                == PaymentStatus.SUCCESS) {

            return booking.getPayment();
        }

        if (booking.getStatus()
                != BookingStatus.PAYMENT_PENDING) {

            throw new IllegalStateException(
                    "Booking is not in PAYMENT_PENDING state. Current: "
                            + booking.getStatus()
            );
        }

        Payment payment =
                paymentService.processPayment(booking);

        booking.setPayment(payment);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            booking.setStatus(
                    BookingStatus.CONFIRMED
            );

            seatLockManager.markSeatsBooked(
                    booking.getShowSeats()
            );

        } else {

            booking.setStatus(
                    BookingStatus.FAILED
            );

            seatLockManager.releaseSeats(
                    booking.getShowSeats()
            );
        }

        return payment;
    }

    /**
     * Cancels a confirmed booking.
     */
    public void cancelBooking(String bookingId) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Booking not found: " + bookingId
            );
        }

        if (booking.getStatus()
                != BookingStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Only CONFIRMED bookings can be cancelled. Current: "
                            + booking.getStatus()
            );
        }

        seatLockManager.releaseSeats(
                booking.getShowSeats()
        );

        booking.setStatus(
                BookingStatus.CANCELLED
        );
    }

    public Booking getBooking(String bookingId) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Booking not found: " + bookingId
            );
        }

        return booking;
    }
}