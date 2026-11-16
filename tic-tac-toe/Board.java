public class Board {

    private static Board instance;
    private char[][] board;
    private int size;

    private Board(int size) {
        this.size = size;
        board = new char[size][size];
        initialize();
    }

    private void initialize() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = '.';
            }
        }
    }

    public static Board getInstance(int size) {
        if (instance == null) {
            instance = new Board(size);
        }
        return instance;
    }

    public boolean isPossible(int x, int y) {
        return board[x][y] == '.';
    }

    public void placeMove(int x, int y, char symbol) {
        board[x][y] = symbol;
    }

    public boolean isWon(char symbol) {

        // rows
        for (int i = 0; i < size; i++) {
            boolean win = true;
            for (int j = 0; j < size; j++) {
                if (board[i][j] != symbol) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }

        // columns
        for (int j = 0; j < size; j++) {
            boolean win = true;
            for (int i = 0; i < size; i++) {
                if (board[i][j] != symbol) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }

        // main diagonal
        boolean win = true;
        for (int i = 0; i < size; i++) {
            if (board[i][i] != symbol) {
                win = false;
                break;
            }
        }
        if (win) return true;

        // anti diagonal
        win = true;
        for (int i = 0; i < size; i++) {
            if (board[i][size - i - 1] != symbol) {
                win = false;
                break;
            }
        }

        return win;
    }

    public void printBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

