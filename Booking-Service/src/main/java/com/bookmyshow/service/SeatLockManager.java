package com.bookmyshow.service;

import com.bookmyshow.enums.SeatStatus;
import com.bookmyshow.model.Show;
import com.bookmyshow.model.ShowSeat;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * SeatLockManager handles all seat-level locking concerns:
 * - Per-show locks (striped locking — different shows don't contend)
 * - Per-seat locks with sorted acquisition (prevents deadlock)
 * - Lock expiration (5-minute TTL)
 * - Atomic check-and-lock for seat availability
 *
 * This is an internal component of BookingService, not a separate service.
 * It encapsulates all concurrency logic for seat management.
 */


public class SeatLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> showLocks =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ReentrantLock> seatLocks =
            new ConcurrentHashMap<>();

    private final ShowService showService;

    private final ScheduledExecutorService scheduler;

    private static final SeatLockManager INSTANCE = new SeatLockManager();

    private SeatLockManager() {
        this.showService = ShowService.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static SeatLockManager getInstance() {
        return INSTANCE;
    }

    // ---------------- Scheduler ----------------

    public void startExpiryScheduler(
            Supplier<Collection<Show>> showsSupplier,
            long period,
            TimeUnit unit) {

        scheduler.scheduleAtFixedRate(() -> {
            try {
                releaseExpiredLock();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, period, unit);
    }

    private void releaseExpiredLock() {
        List<Show> showList = showService.getAllShows();

        for (Show show : showList) {
            for (ShowSeat seat : show.getShowSeats()) {

                if (seat.getStatus() == SeatStatus.LOCKED
                        && seat.getLockExpiryTime() != null
                        && LocalDateTime.now()
                        .isAfter(seat.getLockExpiryTime())) {

                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setLockedBy(null);
                    seat.setLockExpiryTime(null);
                }
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    // ---------------- Locks ----------------

    public ReentrantLock getShowLock(String showId) {
        return showLocks.computeIfAbsent(
                showId,
                k -> new ReentrantLock()
        );
    }

    public void acquireSeatLocks(
            String showId,
            List<String> sortedSeatIds) {

        for (String seatId : sortedSeatIds) {

            String lockKey = showId + ":" + seatId;

            ReentrantLock seatLock =
                    seatLocks.computeIfAbsent(
                            lockKey,
                            k -> new ReentrantLock()
                    );

            seatLock.lock();
        }
    }

    public void releaseSeatLocks(
            String showId,
            List<String> sortedSeatIds) {

        for (int i = sortedSeatIds.size() - 1; i >= 0; i--) {

            String lockKey =
                    showId + ":" + sortedSeatIds.get(i);

            ReentrantLock seatLock = seatLocks.get(lockKey);

            if (seatLock != null
                    && seatLock.isHeldByCurrentThread()) {

                seatLock.unlock();
            }
        }
    }

    // ---------------- Seat Operations ----------------

    public List<ShowSeat> resolveShowSeats(
            Show show,
            List<String> seatIds) {

        List<ShowSeat> requestedSeats = new ArrayList<>();

        for (String seatId : seatIds) {

            ShowSeat found = null;

            for (ShowSeat ss : show.getShowSeats()) {

                if (ss.getSeat().getLabel().equals(seatId)
                        || ss.getSeat().getId().equals(seatId)) {

                    found = ss;
                    break;
                }
            }

            if (found == null) {
                throw new IllegalArgumentException(
                        "Seat not found in this show: " + seatId
                );
            }

            requestedSeats.add(found);
        }

        return requestedSeats;
    }

    public void releaseExpiredLocks(
            List<ShowSeat> showSeats) {

        for (ShowSeat ss : showSeats) {

            if (ss.getStatus() == SeatStatus.LOCKED
                    && ss.getLockExpiryTime() != null
                    && LocalDateTime.now()
                    .isAfter(ss.getLockExpiryTime())) {

                ss.setStatus(SeatStatus.AVAILABLE);
                ss.setLockedBy(null);
                ss.setLockExpiryTime(null);
            }
        }
    }

    public void checkAllAvailable(
            List<ShowSeat> requestedSeats) {

        for (ShowSeat ss : requestedSeats) {

            if (ss.getStatus() != SeatStatus.AVAILABLE) {

                throw new IllegalStateException(
                        "Seat " + ss.getSeat().getLabel()
                                + " is not available. Current status: "
                                + ss.getStatus()
                );
            }
        }
    }

    public void lockSeats(
            List<ShowSeat> requestedSeats,
            String userId) {

        for (ShowSeat ss : requestedSeats) {

            ss.setStatus(SeatStatus.LOCKED);
            ss.setLockedBy(userId);
            ss.setLockExpiryTime(
                    LocalDateTime.now().plusMinutes(5)
            );
        }
    }

    public void releaseSeats(
            List<ShowSeat> showSeats) {

        for (ShowSeat ss : showSeats) {

            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setLockedBy(null);
            ss.setLockExpiryTime(null);
        }
    }

    public void markSeatsBooked(
            List<ShowSeat> showSeats) {

        for (ShowSeat ss : showSeats) {

            ss.setStatus(SeatStatus.BOOKED);
            ss.setLockedBy(null);
            ss.setLockExpiryTime(null);
        }
    }
}