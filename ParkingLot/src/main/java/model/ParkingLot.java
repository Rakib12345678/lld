package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class ParkingLot {

    private static final ParkingLot INSTANCE =
            new ParkingLot();

    private final List<ParkingFloor> floors;

    private final Map<String, Ticket> activeTickets;

    private final FeeStrategy pricingStrategy;

    private ParkingLot() {

        floors = new ArrayList<>();
        activeTickets = new ConcurrentHashMap<>();

        pricingStrategy =
                new HourlyPricingStrategy();
    }

    public static ParkingLot getInstance() {
        return INSTANCE;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public Ticket parkVehicle(Vehicle vehicle) {

        for (ParkingFloor floor : floors) {

            ParkingSpot spot =
                    floor.findAvailableSpot(vehicle);

            if (spot != null && spot.park(vehicle)) {

                String ticketId =
                        UUID.randomUUID().toString();

                Ticket ticket =
                        new Ticket(
                                ticketId,
                                vehicle,
                                spot);

                activeTickets.put(ticketId, ticket);

                return ticket;
            }
        }

        throw new RuntimeException(
                "No parking spot available");
    }

    public double unparkVehicle(String ticketId) {

        Ticket ticket =
                activeTickets.get(ticketId);

        if (ticket == null) {
            throw new RuntimeException(
                    "Invalid ticket");
        }

        synchronized (ticket) {

            if (ticket.getStatus() == TicketStatus.PAID) {
                throw new RuntimeException(
                        "Vehicle already exited");
            }

            double price =
                    pricingStrategy.calculateFees(ticket);

            ticket.getSpot().removeVehicle();

            ticket.close();

            activeTickets.remove(ticketId);

            return price;
        }
    }
}