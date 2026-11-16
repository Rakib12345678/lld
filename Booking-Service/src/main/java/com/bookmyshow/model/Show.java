package com.bookmyshow.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final List<ShowSeat> showSeats;

    public Show(String id, Movie movie, Screen screen, LocalDateTime startTime) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Show ID cannot be null or blank");
        if (movie == null) throw new IllegalArgumentException("Movie cannot be null");
        if (screen == null) throw new IllegalArgumentException("Screen cannot be null");
        if (startTime == null) throw new IllegalArgumentException("Start time cannot be null");
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.showSeats = new ArrayList<>();
    }

    public String getId() { return id; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public LocalDateTime getStartTime() { return startTime; }

    /**
     * Returns the ID of the theatre this show belongs to (via screen).
     * Returns null if the screen has not yet been added to a theatre.
     */
    public String getTheatreId() {
        Theatre theatre = screen.getTheatre();
        return theatre == null ? null : theatre.getId();
    }

    public void addShowSeat(ShowSeat showSeat) {
        if (showSeat == null) throw new IllegalArgumentException("ShowSeat cannot be null");
        showSeats.add(showSeat);
    }

    public List<ShowSeat> getShowSeats() {
        return Collections.unmodifiableList(showSeats);
    }
}