package com.rocky.meeting.calendar.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a reminder for a meeting.
 * Immutable value object — no sent flag.
 * The scheduler dynamically determines when to send reminders.
 */
public final class Reminder {
    public enum ReminderType {
        EMAIL,
        NOTIFICATION,
        SOUND
    }

    private final String id;
    private final ReminderType type;
    private final Duration timeBeforeMeeting;

    public Reminder(ReminderType type, Duration timeBeforeMeeting) {
        this.id = UUID.randomUUID().toString();
        this.type = Objects.requireNonNull(type, "Reminder type must not be null");
        this.timeBeforeMeeting = Objects.requireNonNull(timeBeforeMeeting, "Time before meeting must not be null");
    }

    public Reminder(String id, ReminderType type, Duration timeBeforeMeeting) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.type = Objects.requireNonNull(type, "Reminder type must not be null");
        this.timeBeforeMeeting = Objects.requireNonNull(timeBeforeMeeting, "Time before meeting must not be null");
    }

    public String getId() {
        return id;
    }

    public ReminderType getType() {
        return type;
    }

    public Duration getTimeBeforeMeeting() {
        return timeBeforeMeeting;
    }

    /**
     * Returns the time at which this reminder should trigger, given the meeting start time.
     */
    public LocalDateTime getTriggerTime(LocalDateTime meetingStartTime) {
        return meetingStartTime.minus(timeBeforeMeeting);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(id, reminder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", timeBeforeMeeting=" + timeBeforeMeeting +
                '}';
    }
}