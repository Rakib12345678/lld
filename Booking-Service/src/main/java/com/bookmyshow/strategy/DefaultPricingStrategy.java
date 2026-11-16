package com.bookmyshow.strategy;

import com.bookmyshow.enums.SeatType;
import com.bookmyshow.model.Seat;

/**
 * Default pricing: flat rates per seat type.
 * REGULAR = ₹200, PREMIUM = ₹300, RECLINER = ₹500
 */
public class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Seat seat) {
        return switch (seat.getSeatType()) {
            case REGULAR -> 200;
            case PREMIUM -> 300;
            case RECLINER -> 500;
        };
    }
}