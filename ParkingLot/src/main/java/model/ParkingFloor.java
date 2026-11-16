package model;

import java.util.List;

class ParkingFloor {

    private final int floorNumber;

    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = spots;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public ParkingSpot findAvailableSpot(Vehicle vehicle) {

        for (ParkingSpot spot : spots) {

            if (spot.getVehicle()==null
                    && canFit(vehicle, spot)) {

                return spot;
            }
        }

        return null;
    }

    private boolean canFit(Vehicle vehicle, ParkingSpot spot) {

        return vehicle.getVehicleSize().ordinal() <= spot.getVehicleSize().ordinal();
    }
}