package model;

import enums.VehicleSize;

public abstract class Vehicle {
    VehicleSize vehicleSize;
    String no;

    public Vehicle(VehicleSize vehicleSize, String no) {
        this.vehicleSize = vehicleSize;
        this.no = no;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }

    public void setVehicleSize(VehicleSize vehicleSize) {
        this.vehicleSize = vehicleSize;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }
}
