
public class StandardDice implements Dice {
    private int sides;
    public StandardDice(int sides) {
        this.sides = sides;
    }

    @Override
    public int roll() {
        return (int) (Math.random() * sides) + 1;
    }
}
