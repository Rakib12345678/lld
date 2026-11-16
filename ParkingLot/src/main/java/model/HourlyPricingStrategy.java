package model;

import enums.VehicleSize;

class HourlyPricingStrategy implements FeeStrategy {

    @Override
    public double calculateFees(Ticket ticket) {

        long endTime = System.currentTimeMillis();

        long duration =
                endTime - ticket.getEntry();

        long hours =
                (duration / (60 * 60 * 1000)) + 1;

        return hours * getHourlyRate(
                ticket.getVehicle().getVehicleSize());
    }

    private double getHourlyRate(VehicleSize type) {

        return switch (type) {

            case SMALL -> 20;
            case MEDIUM -> 50;
            case LARGE -> 100;
        };
    }
}