import enums.Direction;
import exceptions.ElevatorException;
import model.Elevator;

import java.util.HashMap;
import java.util.List;

public class ZoneBasedStrategy implements DispatchStrategy {
    private final HashMap<Integer, Integer> zoneAssignments;

    public ZoneBasedStrategy(int totalFloors, int numElevators) {
        this.zoneAssignments = new HashMap<>();
        int floorsPerZone = totalFloors / numElevators;
        int remainder = totalFloors % numElevators;

        int floorStart = 1;
        for (int i = 0; i < numElevators; i++) {
            int zoneSize = floorsPerZone + (i < remainder ? 1 : 0);
            for (int f = floorStart; f < floorStart + zoneSize; f++) {
                zoneAssignments.put(f, i);
            }
            floorStart += zoneSize;
        }
    }

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int floor, Direction direction) {
        Integer assignedIndex = zoneAssignments.get(floor);
        if (assignedIndex != null && assignedIndex < elevators.size()) {
            Elevator assigned = elevators.get(assignedIndex);
            if (assigned.getState() != enums.ElevatorState.OUT_OF_SERVICE) {
                return assigned;
            }
        }
        // Fallback: find the nearest available elevator
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;
        for (Elevator elevator : elevators) {
            if (elevator.getState() != enums.ElevatorState.OUT_OF_SERVICE) {
                int distance = Math.abs(elevator.getCurrentFloor() - floor);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = elevator;
                }
            }
        }
        if (nearest == null) {
            throw new ElevatorException("No available elevator for floor " + floor);
        }
        return nearest;
    }
}