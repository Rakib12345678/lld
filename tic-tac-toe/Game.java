import java.util.ArrayList;
import java.util.List;

import java.util.*;

public class Game {

    private List<Player> players = new ArrayList<>();
    private Board board;
    private int currentPlayerIndex = 0;

    public Game(int boardSize) {
        board = Board.getInstance(boardSize);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void startGame() {

        Scanner sc = new Scanner(System.in);
        int moves = 0;
        int maxMoves = boardSize() * boardSize();

        while (true) {
            Player currentPlayer = players.get(currentPlayerIndex);

            board.printBoard();
            currentPlayer.execute("Enter row and column (0 based):");

            int x = sc.nextInt();
            int y = sc.nextInt();

            if (!board.isPossible(x, y)) {
                currentPlayer.execute("Invalid move, try again.");
                continue;
            }

            board.placeMove(x, y, currentPlayer.getPlayerSymbol());
            moves++;

            if (board.isWon(currentPlayer.getPlayerSymbol())) {
                board.printBoard();
                currentPlayer.execute("🎉 You WON!");
                break;
            }

            if (moves == maxMoves) {
                board.printBoard();
                System.out.println("🤝 Game Draw!");
                break;
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    private int boardSize() {
        return 3; // fixed for TicTacToe
    }
}

