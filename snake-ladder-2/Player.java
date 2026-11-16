public class Player {
    private String name;
    private int position;
    private int consecutiveSixes;

    public Player(String name, int position) {
        this.name = name;
        this.position = position;
        this.consecutiveSixes = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getConsecutiveSixes() {
        return consecutiveSixes;
    }

    public void setConsecutiveSixes(int consecutiveSixes) {
        this.consecutiveSixes = consecutiveSixes;
    }
}
