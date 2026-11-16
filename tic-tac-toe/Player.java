public class Player implements Observer {

    private String id;
    private String name;
    private char playerSymbol;

    public Player(String id, String name, char playerSymbol) {
        this.id = id;
        this.name = name;
        this.playerSymbol = playerSymbol;
    }

    @Override
    public void execute(String message) {
        System.out.println(name + ": " + message);
    }

    public String getName() {
        return name;
    }

    public char getPlayerSymbol() {
        return playerSymbol;
    }
}
