import java.util.Arrays;

public class SnakeAndLadderGame {

    public static void main(String[] args) {
        // Create Board (Singleton)
        Board board = Board.getInstance(100);

        // Add Snakes
        board.addSnake(16, 6);
        board.addSnake(48, 26);
        board.addSnake(49, 11);
        board.addSnake(56, 53);
        board.addSnake(62, 19);
        board.addSnake(64, 60);
        board.addSnake(87, 24);
        board.addSnake(93, 73);
        board.addSnake(95, 75);
        board.addSnake(98, 78);

        // Add Ladders
        board.addLadder(1, 38);
        board.addLadder(4, 14);
        board.addLadder(9, 31);
        board.addLadder(21, 42);
        board.addLadder(28, 84);
        board.addLadder(36, 44);
        board.addLadder(51, 67);
        board.addLadder(71, 91);
        board.addLadder(80, 100);

        // Create Game
        Game game = new Game();

        // Create Players
        Player p1 = new Player("1", "Alice");
        Player p2 = new Player("2", "Bob");

        // Add players to game
        game.addPlayer(p1);
        game.addPlayer(p2);

        // Create Dice (size 6)
        Dice dice = new Dice(6);

        System.out.println("✅ Game Started!");

        // Game Loop
        while (true) {

            Player current = game.getCurrentPlayer();
            int roll = dice.rollDice();

            Move move = new MoveCommand(current, roll, board);
            move.execute();

            game.notifyPlayers(
                    current.getName() + " rolled a " + roll +
                            " → moved to position " + current.getPosition()
            );

            // Check if player won
            if (current.getPosition() == board.getSize()) {
                game.notifyPlayers("🎉 " + current.getName() + " wins the game!");
                break;
            }

            game.nextTurn();
        }
    }
}
