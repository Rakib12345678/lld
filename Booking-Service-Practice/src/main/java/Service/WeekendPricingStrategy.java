package Service;

import Model.Seat;

public class WeekendPricingStrategy implements PricingStrategy{
    private DefaultPricing basePricing;
    private static final double SURCHARGE_PERCENT = 0.20;

    public WeekendPricingStrategy(DefaultPricing basePricing) {
        this.basePricing = basePricing;
    }

    @Override
    public double calcaulatePrice(Seat seat) {
        double basePrice=basePricing.calcaulatePrice(seat);
        return basePrice * (1 + SURCHARGE_PERCENT);
    }
}
