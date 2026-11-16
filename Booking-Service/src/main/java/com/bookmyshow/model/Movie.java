package com.bookmyshow.model;

public class Movie {
    private final String id;
    private final String title;
    private final String genre;
    private final int durationMinutes;

    public Movie(String id, String title, String genre, int durationMinutes) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Movie ID cannot be null or blank");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Movie title cannot be null or blank");
        if (durationMinutes <= 0) throw new IllegalArgumentException("Duration must be positive");
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDurationMinutes() { return durationMinutes; }
}