import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
       HashMap<Integer, Integer> snakes = new HashMap<>();
        HashMap<Integer, Integer> ladders = new HashMap<>();


        // -------- BOARD SIZE --------
        System.out.print("Enter board size: ");
        int boardSize = sc.nextInt();



        // -------- SNAKES --------
        System.out.print("Enter number of snakes: ");
        int snakeCount = sc.nextInt();

        for (int i = 1; i <= snakeCount; i++) {
            System.out.println("Enter snake " + i + " start and end:");

            int start = sc.nextInt();
            int end = sc.nextInt();

            if (start <= end) {
                System.out.println("❌ Snake start must be GREATER than destination.");
                i--;
                continue;
            }

            if (start > boardSize || end < 1) {
                System.out.println("❌ Snake positions must be within board range.");
                i--;
                continue;
            }

            snakes.put(start, end);
        }

        // -------- LADDERS --------
        System.out.print("Enter number of ladders: ");
        int ladderCount = sc.nextInt();

        for (int i = 1; i <= ladderCount; i++) {
            System.out.println("Enter ladder " + i + " start and end:");

            int start = sc.nextInt();
            int end = sc.nextInt();

            if (start >= end) {
                System.out.println("❌ Ladder destination must be GREATER than start.");
                i--;
                continue;
            }

            if (end > boardSize || start < 1) {
                System.out.println("❌ Ladder positions must be within board range.");
                i--;
                continue;
            }

            ladders.put(start, end);
        }
        Board board = new Board(boardSize, snakes, ladders);
        // -------- PLAYERS --------
        Queue<Player> players = new LinkedList<>();
        players.add(new Player("Player-1", 0));
        players.add(new Player("Player-2", 0));

        Dice dice = new StandardDice(6);

        GameManager gameManager = GameManager.getInstance();

        Game game = gameManager.createGame(
                board,
                dice,
                players,
                new ThreeSixesRule(),
                new ExactRollRule()
        );

        gameManager.startGame(game);
    }
}
