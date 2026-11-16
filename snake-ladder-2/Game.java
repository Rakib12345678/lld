import java.util.Queue;

public class Game {
    private Board board;
    private Queue<Player> players;
    private Dice dice;
    private TurnRule turnRule;
    private MoveRule moveRule;

    public Game(Board board, Queue<Player> players, Dice dice, TurnRule turnRule, MoveRule moveRule) {
        this.board = board;
        this.players = players;
        this.dice = dice;
        this.turnRule = turnRule;
        this.moveRule = moveRule;
    }

    public void start() {

        while (true) {
            Player currentPlayer = players.poll();
            int totalMove = 0;
            boolean extraTurn;

            do {
                int diceValue = dice.roll();
                System.out.println(currentPlayer.getName() + " rolled " + diceValue);

                // update rule state
                turnRule.onRoll(currentPlayer, diceValue);

                // cancel turn on 3 consecutive sixes
                if (turnRule.shouldCancelTurn(currentPlayer)) {
                    System.out.println("❌ Turn cancelled due to three sixes");
                    totalMove = 0;
                    break;
                }

                totalMove += diceValue;

                // check extra turn
                extraTurn = turnRule.shouldExtraTurnGive(currentPlayer, diceValue);

            } while (extraTurn);

            // apply movement rule
            if (moveRule.shouldMoveAllowed(
                    currentPlayer,
                    currentPlayer.getPosition(),
                    totalMove,
                    board)) {

                int nextPosition = currentPlayer.getPosition() + totalMove;

                // resolve snake or ladder
                if (board.isExistSnake(nextPosition)) {
                    nextPosition = board.getSnakeDestination(nextPosition);
                } else if (board.isExistLadder(nextPosition)) {
                    nextPosition = board.getLadderDestination(nextPosition);
                }

                currentPlayer.setPosition(nextPosition);
            }

            // reset rule state
            turnRule.onTurnEnd(currentPlayer);

            System.out.println(
                    currentPlayer.getName() +
                            " moved to " +
                            currentPlayer.getPosition()
            );
            System.out.println("----------------------");

            // WIN condition
            if (currentPlayer.getPosition() == board.getSize()) {
                System.out.println("🎉 " + currentPlayer.getName() + " wins!");
                return;
            }

            // rotate turn
            players.add(currentPlayer);
        }
    }

}
