public class Dice {
    int size;
    public Dice(int size){
        this.size = size;
    }
    public int rollDice(){
        return (int) (Math.random() * size) +1;
    }
}
