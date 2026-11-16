import enums.Direction;
import enums.ElevatorState;
import exceptions.ElevatorException;
import model.Elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElevatorSystem {
    private final List<Elevator> elevators;
    private final List<ElevatorController> controllers;
    private final DispatchStrategy dispatchStrategy;
    private final ExecutorService executorService;

    public ElevatorSystem(int numElevators, int totalFloors, DispatchStrategy dispatchStrategy) {
        this.elevators = new ArrayList<>();
        this.controllers = new ArrayList<>();
        this.dispatchStrategy = dispatchStrategy;
        this.executorService = Executors.newFixedThreadPool(numElevators);

        for (int i = 0; i < numElevators; i++) {
            Elevator elevator = new Elevator(i + 1, totalFloors);
            ElevatorController controller = new ElevatorController(elevator);
            elevators.add(elevator);
            controllers.add(controller);
        }
    }

    public void requestElevator(int floor, Direction direction) {
        Elevator selected = dispatchStrategy.selectElevator(elevators, floor, direction);
        if (selected == null) {
            throw new ElevatorException("No suitable elevator found for floor " + floor);
        }
        selected.addRequest(floor, direction);
    }

    public void start() {
        for (ElevatorController controller : controllers) {
            executorService.submit(controller);
        }
    }

    public void shutdown() {
        for (ElevatorController controller : controllers) {
            controller.stop();
        }
        executorService.shutdown();
    }

    public List<Elevator> getElevators() {
        return elevators;
    }

    public Elevator getElevator(int id) {
        for (Elevator elevator : elevators) {
            if (elevator.getId() == id) {
                return elevator;
            }
        }
        return null;
    }
}