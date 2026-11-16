package com.rocky.meeting.calendar.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a meeting participant.
 * Immutable value object.
 */
public final class Participant {
    private final String id;
    private final String name;
    private final String email;

    public Participant(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.email = Objects.requireNonNull(email, "Email must not be null");
    }

    public Participant(String id, String name, String email) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.email = Objects.requireNonNull(email, "Email must not be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}