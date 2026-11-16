package model;

import enums.Direction;

public interface ElevatorObserver {
    void onElevatorStateChanges(int elevatorId, int currentFloor, Direction direction);
}
