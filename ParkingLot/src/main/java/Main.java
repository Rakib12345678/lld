package model;

import enums.VehicleSize;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // ===================== Setup Parking Lot =====================
        ParkingLot parkingLot = ParkingLot.getInstance();

        // Create spots on floor 0 with different sizes
        List<ParkingSpot> floor0Spots = Arrays.asList(
                new ParkingSpot("A1", VehicleSize.SMALL, null),
                new ParkingSpot("A2", VehicleSize.SMALL, null),
                new ParkingSpot("B1", VehicleSize.MEDIUM, null),
                new ParkingSpot("C1", VehicleSize.LARGE, null)
        );

        ParkingFloor floor0 = new ParkingFloor(0, floor0Spots);
        parkingLot.addFloor(floor0);

        // Create spots on floor 1
        List<ParkingSpot> floor1Spots = Arrays.asList(
                new ParkingSpot("A3", VehicleSize.SMALL, null),
                new ParkingSpot("B2", VehicleSize.MEDIUM, null),
                new ParkingSpot("C2", VehicleSize.LARGE, null)
        );

        ParkingFloor floor1 = new ParkingFloor(1, floor1Spots);
        parkingLot.addFloor(floor1);

        System.out.println("========= Parking Lot Initialized =========");
        System.out.println("Floors: 2 | Total Spots: 7");
        System.out.println("Small: A1, A2, A3 | Medium: B1, B2 | Large: C1, C2");
        System.out.println();

        // ===================== Park Vehicles =====================
        System.out.println("========= Park Vehicles =========");

        // Park a Bike (SMALL) — should go to A1
        Vehicle bike = new Bike("BIKE-001", VehicleSize.SMALL);
        Ticket ticket1 = parkingLot.parkVehicle(bike);
        System.out.println("Bike parked → Ticket: " + ticket1.getId()
                + " | Spot: " + ticket1.getSpot().getId()
                + " | Entry: " + ticket1.getEntryTime());

        // Park a Car (MEDIUM) — should go to B1
        Vehicle car = new Car(VehicleSize.MEDIUM, "CAR-001");
        Ticket ticket2 = parkingLot.parkVehicle(car);
        System.out.println("Car parked   → Ticket: " + ticket2.getId()
                + " | Spot: " + ticket2.getSpot().getId()
                + " | Entry: " + ticket2.getEntryTime());

        // Park a Truck (LARGE) — should go to C1
        Vehicle truck = new Truck(VehicleSize.LARGE, "TRUCK-001");
        Ticket ticket3 = parkingLot.parkVehicle(truck);
        System.out.println("Truck parked → Ticket: " + ticket3.getId()
                + " | Spot: " + ticket3.getSpot().getId()
                + " | Entry: " + ticket3.getEntryTime());

        // Park another Bike (SMALL) — should go to A2
        Vehicle bike2 = new Bike("BIKE-002", VehicleSize.SMALL);
        Ticket ticket4 = parkingLot.parkVehicle(bike2);
        System.out.println("Bike2 parked → Ticket: " + ticket4.getId()
                + " | Spot: " + ticket4.getSpot().getId()
                + " | Entry: " + ticket4.getEntryTime());
        System.out.println();

        // ===================== Unpark Vehicles =====================
        System.out.println("========= Unpark Vehicles =========");

        double fee1 = parkingLot.unparkVehicle(ticket1.getId());
        System.out.println("Bike unparked → Fee: ₹" + fee1 + " | Spot A1 is now free");

        double fee2 = parkingLot.unparkVehicle(ticket2.getId());
        System.out.println("Car unparked  → Fee: ₹" + fee2 + " | Spot B1 is now free");

        double fee3 = parkingLot.unparkVehicle(ticket3.getId());
        System.out.println("Truck unparked → Fee: ₹" + fee3 + " | Spot C1 is now free");

        System.out.println();

        // ===================== Try to unpark the same ticket twice =====================
        System.out.println("========= Duplicate Exit Attempt =========");
        try {
            parkingLot.unparkVehicle(ticket1.getId());
        } catch (RuntimeException e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        System.out.println();
        System.out.println("========= All Done =========");
    }
}