package com.bookmyshow.model;

public class User {
    private final String id;
    private final String name;
    private final String email;

    public User(String id, String name, String email) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("User ID cannot be null or blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("User name cannot be null or blank");
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}