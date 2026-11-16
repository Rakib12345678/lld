package com.bookmyshow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Theatre {
    private final String id;
    private final String name;
    private final String city;
    private final List<Screen> screens;

    public Theatre(String id, String name, String city) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Theatre ID cannot be null or blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Theatre name cannot be null or blank");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City cannot be null or blank");
        this.id = id;
        this.name = name;
        this.city = city;
        this.screens = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }

    public void addScreen(Screen screen) {
        if (screen == null) throw new IllegalArgumentException("Screen cannot be null");
        // Establish bidirectional link so Screen.getTheatre() works.
        screen.setTheatre(this);
        screens.add(screen);
    }

    public List<Screen> getScreens() {
        return Collections.unmodifiableList(screens);
    }
}