import java.util.HashMap;

public class Board {
    private int size;
    private HashMap<Integer, Integer> snakes;
    private HashMap<Integer, Integer> ladders;


    public Board(int size,HashMap<Integer, Integer> snakes,HashMap<Integer, Integer> ladders) {
        this.size = size;
        this.snakes = snakes;
        this.ladders = ladders;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSnakes(int start) {
        return snakes.get(start);
    }
    public void setSnakes(HashMap<Integer, Integer> snakes) {
        this.snakes = snakes;
    }
    public void setLadders(HashMap<Integer, Integer> ladders) {
        this.ladders = ladders;
    }
    public boolean isExistSnake(int start) {
        return snakes.containsKey(start);
    }
    public boolean isExistLadder(int start) {
        return ladders.containsKey(start);
    }
    public int getSnakeDestination(int start) {
        return snakes.get(start);
    }
    public int getLadderDestination(int start) {
        return ladders.get(start);
    }
}
