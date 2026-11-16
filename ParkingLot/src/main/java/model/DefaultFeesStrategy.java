package model;

import enums.VehicleSize;

public class DefaultFeesStrategy implements FeeStrategy{
    @Override
    public double calculateFees(Ticket ticket) {
        return switch(ticket.getVehicle().getVehicleSize()){
            case SMALL -> 10;
            case MEDIUM -> 20;
            case LARGE -> 30;
        };
    }
}