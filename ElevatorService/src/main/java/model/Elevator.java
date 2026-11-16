package model;

import enums.Direction;
import enums.ElevatorState;
import exceptions.ElevatorException;

import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class Elevator {
    private final int id;
    private int currentFloor;
    private Direction direction;
    private ElevatorState state;
    private final Door door;
    private final Display display;
    // Ascending order: ceiling() gives us the next floor above
    private final TreeSet<Integer> upRequests;
    // Descending order: ceiling() gives us the next floor below (highest first)
    private final TreeSet<Integer> downRequests;
    // Thread-safe list for observers that may be modified during iteration
    private final CopyOnWriteArrayList<ElevatorObserver> observers;
    private final int totalFloors;

    public Elevator(int id, int totalFloors) {
        this.id = id;
        this.currentFloor = 1;
        this.direction = Direction.IDLE;
        this.state = ElevatorState.IDLE;
        this.door = new Door();
        this.display = new Display(id);
        this.upRequests = new TreeSet<>();
        // Reverse order so ceiling() returns the next floor BELOW current
        this.downRequests = new TreeSet<>(Collections.reverseOrder());
        this.observers = new CopyOnWriteArrayList<>();
        this.totalFloors = totalFloors;
        addObserver(display);
    }

    public synchronized void addRequest(int floor, Direction direction) {
        if (state == ElevatorState.OUT_OF_SERVICE) {
            throw new ElevatorException("Elevator " + id + " is out of service");
        }
        if (floor < 1 || floor > totalFloors) {
            throw new ElevatorException("Floor " + floor + " is out of range (1-" + totalFloors + ")");
        }
        if (floor > currentFloor) {
            upRequests.add(floor);
        } else if (floor < currentFloor) {
            downRequests.add(floor);
        } else {
            // floor == currentFloor
            if (state == ElevatorState.IDLE) {
                openDoor();
                closeDoor();
                return;
            } else if (state == ElevatorState.MOVING_DOWN) {
                upRequests.add(floor);
            } else if (state == ElevatorState.MOVING_UP) {
                downRequests.add(floor);
            } else {
                return;
            }
        }
        System.out.println("Elevator " + id + " received request for floor " + floor);
    }

    public synchronized int getNextStop() {
        if (direction == Direction.UP) {
            Integer next = upRequests.ceiling(currentFloor);
            if (next != null) {
                return next;
            }
        }
        if (direction == Direction.DOWN) {
            Integer next = downRequests.ceiling(currentFloor);
            if (next != null) {
                return next;
            }
        }
        return -1;
    }

    public synchronized void moveToFloor(int floor) {
        currentFloor = floor;
        if (direction == Direction.UP) {
            state = ElevatorState.MOVING_UP;
        } else if (direction == Direction.DOWN) {
            state = ElevatorState.MOVING_DOWN;
        }
        notifyObservers();
    }

    public void openDoor() {
        door.setDoorState(enums.DoorState.OPEN);
        state = ElevatorState.DOOR_OPEN;
        System.out.println("Elevator " + id + " arrived at floor " + currentFloor + ", opening door");
    }

    public void closeDoor() {
        door.setDoorState(enums.DoorState.CLOSED);
        System.out.println("Elevator " + id + " door closed");
    }

    public synchronized void removeCurrentFloorFromRequests() {
        upRequests.remove(currentFloor);
        downRequests.remove(currentFloor);
    }

    public void addObserver(ElevatorObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorStateChanges(id, currentFloor, direction);
        }
    }

    public synchronized boolean hasRequests() {
        return !upRequests.isEmpty() || !downRequests.isEmpty();
    }

    public synchronized boolean hasUpRequests() {
        return !upRequests.isEmpty();
    }

    public synchronized boolean hasDownRequests() {
        return !downRequests.isEmpty();
    }

    public int getId() {
        return id;
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    public synchronized void setDirection(Direction direction) {
        this.direction = direction;
    }

    public synchronized ElevatorState getState() {
        return state;
    }

    public synchronized void setState(ElevatorState state) {
        this.state = state;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public Display getDisplay() {
        return display;
    }
}