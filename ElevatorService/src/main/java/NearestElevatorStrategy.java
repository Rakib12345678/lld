import enums.Direction;
import model.Elevator;

import java.util.List;

public class NearestElevatorStrategy implements DispatchStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int floor, Direction direction) {
        Elevator bestElevator = null;
        int maxScore = Integer.MIN_VALUE;

        for (Elevator elevator : elevators) {
            int score;

            if (elevator.getDirection() == Direction.UP && direction == Direction.UP) {
                if (elevator.getCurrentFloor() <= floor) {
                    score = floor - elevator.getCurrentFloor() + 100;
                } else {
                    score = 1;
                }
            } else if (elevator.getDirection() == Direction.DOWN && direction == Direction.DOWN) {
                if (elevator.getCurrentFloor() >= floor) {
                    score = elevator.getCurrentFloor() - floor + 100;
                } else {
                    score = 1;
                }
            } else if (elevator.getDirection() == Direction.IDLE) {
                score = Math.abs(floor - elevator.getCurrentFloor());
            } else {
                score = 1;
            }

            if (score > maxScore) {
                maxScore = score;
                bestElevator = elevator;
            }
        }

        return bestElevator;
    }
}