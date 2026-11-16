package model;


import enums.VehicleSize;

public class ParkingSpot {
    private String id;
    private VehicleSize vehicleSize;
    private Vehicle vehicle;

    public ParkingSpot(String id, VehicleSize vehicleSize, Vehicle vehicle) {
        this.id = id;
        this.vehicleSize = vehicleSize;
        this.vehicle = vehicle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }

    public void setVehicleSize(VehicleSize vehicleSize) {
        this.vehicleSize = vehicleSize;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public boolean park(Vehicle vehicle) {
        if (this.vehicle != null) {
            return false;
        }
        this.vehicle = vehicle;
        return true;
    }

    public void removeVehicle() {
        this.vehicle = null;
    }
}