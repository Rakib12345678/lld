package Service;

import Model.Show;
import Model.ShowSeat;
import enums.SeatStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SeatLockManager {
    private final ConcurrentHashMap<String, ReentrantLock>showLock=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,ReentrantLock>seatLock=new ConcurrentHashMap<>();

    private static final SeatLockManager instance=new SeatLockManager();
    public static SeatLockManager getInstance(){
        return instance;
    }


    public ReentrantLock getShowLock(String showId){
        return showLock.computeIfAbsent(showId,k->new ReentrantLock());
    }

    public void AcquireSeatLock(String showId, List<String> showSeatIds){
        for(String id:showSeatIds){
            String key=showId+":"+id;
           ReentrantLock seatlock= seatLock.computeIfAbsent(key,k->new ReentrantLock());
           seatlock.lock();

        }
    }

    public void ReleaseSeatLock(String showId,List<String> showSeatIds){
        for(String id:showSeatIds){
            String key=showId+":"+id;
            ReentrantLock seatlock=seatLock.get(key);
            if(seatlock!=null && seatlock.isHeldByCurrentThread()){
                seatlock.unlock();
            }
        }
    }



    public List<ShowSeat> resolveShowSeats(Show show, List<String> seatIds) {
        List<ShowSeat> requestedSeats = new ArrayList<>();
        for (String seatId : seatIds) {
            ShowSeat found = null;
            for (ShowSeat ss : show.getShowSeats()) {
                if (ss.getSeat().get().equals(seatId) || ss.getSeat().getId().equals(seatId)) {
                    found = ss;
                    break;
                }
            }
            if (found == null) {
                throw new IllegalArgumentException("Seat not found in this show: " + seatId);
            }
            requestedSeats.add(found);
        }
        return requestedSeats;
    }


    /**
     * Releases expired locks for the given seats.
     * Locks expire after 5 minutes to prevent deadlocks if a user
     * initiates booking but never completes payment.
     */
    public void releaseExpiredLocks(List<ShowSeat> showSeats) {
        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == SeatStatus.RESERVED
                    && ss.getExpiry() != null
                    && LocalDateTime.now().isAfter(ss.getExpiry())) {
                ss.setStatus(SeatStatus.AVAILABLE);

                ss.setExpiry(null);
            }
        }
    }

    /**
     * Checks that ALL requested seats are AVAILABLE.
     * Throws IllegalStateException if any seat is not available.
     */
    public void checkAllAvailable(List<ShowSeat> requestedSeats) {
        for (ShowSeat ss : requestedSeats) {
            if (ss.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + ss.getSeat()
                        + " is not available. Current status: " + ss.getStatus());
            }
        }
    }

    public void lockSeats(List<ShowSeat> requestedSeats, String userId) {
        for (ShowSeat ss : requestedSeats) {
            ss.setStatus(SeatStatus.RESERVED);

            ss.setExpiry(LocalDateTime.now().plusMinutes(5));
        }
    }


    /**
     * Releases all seats: sets status to AVAILABLE, clears lock info.
     */
    public void releaseSeats(List<ShowSeat> showSeats) {
        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.AVAILABLE);

            ss.setExpiry(null);
        }
    }

    /**
     * Marks all seats as BOOKED: sets status to BOOKED, clears lock info.
     */
    public void markSeatsBooked(List<ShowSeat> showSeats) {
        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.BOOKED);
            ss.setExpiry(null);
        }
    }
}
