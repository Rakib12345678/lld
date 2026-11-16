package model;

import enums.VehicleSize;

public class Bike extends Vehicle{
   Bike(String no, VehicleSize vehicleSize){
       super(vehicleSize, no);
   }
}