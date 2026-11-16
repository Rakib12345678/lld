package parkingmodule;

import VehicleModule.Vehicle;

public class ParkingSpot {
    private String id;
    private SpotType spotType;
    private Vehicle vehicle;


    public void parkVehicle(Vehicle vehicle){
        this.vehicle=vehicle;
    }

    public boolean isAvailable(){
        return vehicle==null;
    }

    public void unparkVehicle(Vehcle vehicle){
        this.vehicle=null;
    }




}
