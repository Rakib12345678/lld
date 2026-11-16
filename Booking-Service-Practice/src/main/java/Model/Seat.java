package Model;

import enums.SeatType;

public class Seat {
    private String id;
    private SeatType seatType;
    private int row;
    private int col;

    public Seat(String id, SeatType seatType, int row, int col) {
        this.id = id;
        this.seatType = seatType;
        this.row = row;
        this.col = col;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
