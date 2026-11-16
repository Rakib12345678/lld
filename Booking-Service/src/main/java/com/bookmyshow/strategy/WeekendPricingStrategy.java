package com.bookmyshow.strategy;

import com.bookmyshow.model.Seat;

/**
 * Weekend pricing: adds a 20% surcharge on top of the default price.
 * Demonstrates how easily a new pricing strategy can be added
 * without changing any existing code (Open/Closed Principle).
 */
public class WeekendPricingStrategy implements PricingStrategy {
    private static final double SURCHARGE_PERCENT = 0.20;
    private final PricingStrategy baseStrategy;

    public WeekendPricingStrategy(PricingStrategy baseStrategy) {
        this.baseStrategy = baseStrategy;
    }

    @Override
    public double calculatePrice(Seat seat) {
        double basePrice = baseStrategy.calculatePrice(seat);
        return basePrice * (1 + SURCHARGE_PERCENT);
    }
}