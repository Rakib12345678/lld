import enums.Direction;
import enums.ElevatorState;
import model.Elevator;

class ElevatorController implements Runnable {
    private final Elevator elevator;
    private volatile boolean running;

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        this.running = true;
    }

    @Override
    public void run() {
        while (running && elevator.hasRequests()) {
            processRequests();
        }
        elevator.setDirection(Direction.IDLE);
        elevator.setState(ElevatorState.IDLE);
    }

    public void stop() {
        this.running = false;
    }

    private void processRequests() {
        // If the direction is IDLE, pick UP if there are up requests, else DOWN if there are down requests, then return.
        if (elevator.getDirection() == Direction.IDLE) {
            if (elevator.hasUpRequests()) {
                elevator.setDirection(Direction.UP);
            } else if (elevator.hasDownRequests()) {
                elevator.setDirection(Direction.DOWN);
            }
            return;
        }

        // Get the next stop. If it is -1, reverse direction when the other direction has requests,
        // otherwise set the elevator IDLE, then return.
        int nextStop = elevator.getNextStop();
        if (nextStop == -1) {
            if (elevator.getDirection() == Direction.UP && elevator.hasDownRequests()) {
                elevator.setDirection(Direction.DOWN);
            } else if (elevator.getDirection() == Direction.DOWN && elevator.hasUpRequests()) {
                elevator.setDirection(Direction.UP);
            } else {
                elevator.setDirection(Direction.IDLE);
                elevator.setState(ElevatorState.IDLE);
            }
            return;
        }

        // Otherwise move one floor toward the next stop (up or down).
        int currentFloor = elevator.getCurrentFloor();
        if (nextStop > currentFloor) {
            elevator.setDirection(Direction.UP);
            elevator.moveToFloor(currentFloor + 1);
        } else {
            elevator.setDirection(Direction.DOWN);
            elevator.moveToFloor(currentFloor - 1);
        }

        // Simulate travel time between floors
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // If the elevator has reached the next stop, serve that floor (open door, close door, clear the floor from requests).
        if (elevator.getCurrentFloor() == nextStop) {
            serveFloor();
        }
    }

    private void serveFloor() {
        elevator.openDoor();
        // Simulate door open time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        elevator.closeDoor();
        elevator.removeCurrentFloorFromRequests();
    }
}