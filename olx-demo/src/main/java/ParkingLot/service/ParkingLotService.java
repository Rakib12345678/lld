package ParkingLot.service;

import TicketModule.Ticket;
import VehicleModule.Vehicle;

public class ParkingLotService {
    private List<ParkingFloor>floors;
    private HashMap<String ticketId,Ticket>ticketlist;

    public void parkVehicle(Vehicle vehicle){
        for(ParkingFloor parkingfloor :floors){
            {
                parkingspot =fetchAvaiilabelParkingSppot(vehicle);
                parkingspot.parkVehicle(vehicle);
                create
            }
        }
    }


    public void unparkVehicle(Ticket ticket )
    {

    }
}
