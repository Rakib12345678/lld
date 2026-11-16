package model;

import enums.Direction;

public class Display implements ElevatorObserver {
    private int elevatorId;
    private int currentFloor;
    private Direction direction;

    public Display(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 1;
        this.direction = Direction.IDLE;
    }


    @Override
    public void onElevatorStateChanges(int elevatorId, int currentFloor, Direction direction) {
        if (this.elevatorId == elevatorId) {
            this.currentFloor = currentFloor;
            this.direction = direction;
            System.out.println("Elevator " + elevatorId + " is now on floor " + currentFloor + " moving " + direction);
        }
    }
}
