public class ExactRollRule implements MoveRule {

    @Override
    public boolean shouldMoveAllowed(Player player, int currentPosition, int move, Board board) {
        return currentPosition + move <= board.getSize();
    }
}
