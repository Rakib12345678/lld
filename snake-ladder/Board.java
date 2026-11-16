
import java.util.HashMap;
public class Board {
    private  static  Board instance;
    HashMap<Integer,Integer>snakes;
    HashMap<Integer,Integer>ladders;
    int size;
    private Board(int size){
        this.size = size;
        this.snakes = new HashMap<>();
        this.ladders = new HashMap<>();
    }
    public void addSnake(int x,int y){
        snakes.put(x,y);
    }
    public HashMap<Integer,Integer> getSnakes(){
        return snakes;
    }
    public void addLadder(int x,int y){
        ladders.put(x,y);
    }
    public HashMap<Integer,Integer> getLadders(){
        return ladders;
    }
//    public boolean isSnake(int x,int y){
//        return snakes.containsKey(x) && snakes.get(x)==y;
//    }
//    public boolean isLadder(int x,int y){
//        return ladders.containsKey(x) && ladders.get(x)==y;
//    }
    public int getSize(){
        return this.size;
    }
    public static Board getInstance(int size){
        if(instance==null){
            instance = new Board(size);
        }
        return instance;
    }

}
