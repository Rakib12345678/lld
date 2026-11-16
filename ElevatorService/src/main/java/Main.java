import enums.Direction;
import model.Elevator;

public class Main {
    public static void main(String[] args) {
        // ==========================================
        // Demo 1: Single elevator with NearestElevatorStrategy
        // ==========================================
        System.out.println("=== Demo 1: ElevatorSystem with NearestElevatorStrategy ===");
        ElevatorSystem system = new ElevatorSystem(3, 10, new NearestElevatorStrategy());

        // Add some external requests
        system.requestElevator(5, Direction.UP);
        system.requestElevator(3, Direction.DOWN);
        system.requestElevator(7, Direction.UP);
        system.requestElevator(1, Direction.DOWN);

        // Start the system
        system.start();

        // Wait for all requests to complete
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        system.shutdown();
        System.out.println("Elevator system completed all requests.\n");

        // ==========================================
        // Demo 2: Single elevator with ZoneBasedStrategy
        // ==========================================
        System.out.println("=== Demo 2: ElevatorSystem with ZoneBasedStrategy ===");
        ElevatorSystem system2 = new ElevatorSystem(3, 10, new ZoneBasedStrategy(10, 3));

        system2.requestElevator(2, Direction.UP);
        system2.requestElevator(5, Direction.DOWN);
        system2.requestElevator(9, Direction.UP);

        system2.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        system2.shutdown();
        System.out.println("Zone-based elevator system completed all requests.");
    }
}