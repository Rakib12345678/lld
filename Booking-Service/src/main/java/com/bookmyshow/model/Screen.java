package com.bookmyshow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Screen {
    private final String id;
    private final String name;
    private final List<Seat> seats;
    // Back-reference to the parent theatre (set when added via Theatre.addScreen).
    // Allows Show -> Screen -> Theatre lookup without scanning all theatres.
    private Theatre theatre;

    public Screen(String id, String name) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Screen ID cannot be null or blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Screen name cannot be null or blank");
        this.id = id;
        this.name = name;
        this.seats = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }

    /**
     * Returns the parent theatre, or null if this screen has not been added to one yet.
     */
    public Theatre getTheatre() { return theatre; }

    /**
     * Sets the parent theatre. Intended to be called by {@link Theatre#addScreen(Screen)}.
     */
    void setTheatre(Theatre theatre) { this.theatre = theatre; }

    public void addSeat(Seat seat) {
        if (seat == null) throw new IllegalArgumentException("Seat cannot be null");
        seats.add(seat);
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }
}
