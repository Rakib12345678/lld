package com.bookmyshow.strategy;

import com.bookmyshow.model.Show;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class PricingStrategyFactory {

    public static PricingStrategy getStrategy(Show show) {

        if (isWeekend(show.getStartTime())) {
            return new WeekendPricingStrategy(
                    new DefaultPricingStrategy()
            );
        }

        return new DefaultPricingStrategy();
    }

    private static boolean isWeekend(LocalDateTime time) {
        DayOfWeek day = time.getDayOfWeek();

        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }
}