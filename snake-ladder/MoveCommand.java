public class MoveCommand implements Move {
    private Player player;
    private Board board;
    private int step;
    public MoveCommand(Player player,int step,Board board){
        this.player = player;
        this.step = step;
        this.board = board;
    }
    @Override
    public void execute() {
        int oldPosition = player.getPosition();
        int newPosition = oldPosition + step;

        if (newPosition > board.getSize()) {
            newPosition = board.getSize();
        }

        // Snake or ladder check
        if (board.getSnakes().containsKey(newPosition)) {
            System.out.println("🐍 Snake detected at position " + newPosition+"->"+board.getSnakes().get(newPosition));
            newPosition = board.getSnakes().get(newPosition);
        } else if (board.getLadders().containsKey(newPosition)) {
            System.out.println("🚧 Ladder detected at position " + newPosition);
            newPosition = board.getLadders().get(newPosition);
        }

        player.setPosition(newPosition);

    }
}
