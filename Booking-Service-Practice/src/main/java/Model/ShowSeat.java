package Model;

import enums.SeatStatus;

import java.time.LocalDateTime;

public class ShowSeat {
    private String id;
    private SeatStatus status;
    private Seat seat;
    private LocalDateTime expiry;

    public ShowSeat(String id, SeatStatus status, Seat seat, LocalDateTime expiry) {
        this.id = id;
        this.status = status;
        this.seat = seat;
        this.expiry = expiry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}
