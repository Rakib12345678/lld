import java.util.List;
import java.util.Queue;

public class GameManager {
    private static final GameManager instance = new GameManager();
    public static GameManager getInstance() {
        return instance;
    }

    private GameManager() {

    }
    public Game createGame(Board board,
                           Dice dice,
                          Queue<Player> players,
                          TurnRule turnRules,
                           MoveRule moveRules) {

        return new Game(board, players,dice, turnRules, moveRules);
    }

    public void startGame(Game game) {
        game.start();
    }
}
