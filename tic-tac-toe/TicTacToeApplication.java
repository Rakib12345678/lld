public class TicTacToeApplication {
    public static void main(String[] args) throws InterruptedException {
        // Create Game
        Game game = new Game(3);
        Thread.sleep(1000);
        game.addPlayer(new Player("1", "Alice", 'X'));
        game.addPlayer(new Player("2", "Bob", 'O'));
        game.startGame();
    }
}
