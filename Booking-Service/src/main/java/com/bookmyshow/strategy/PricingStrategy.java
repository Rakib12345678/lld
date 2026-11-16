package com.bookmyshow.strategy;

import com.bookmyshow.model.Seat;

/**
 * Strategy Pattern for pricing.
 * Different pricing strategies can be plugged in without modifying BookingService.
 * This demonstrates Open/Closed Principle — open for extension, closed for modification.
 */
@FunctionalInterface
public interface PricingStrategy {
    double calculatePrice(Seat seat);
}