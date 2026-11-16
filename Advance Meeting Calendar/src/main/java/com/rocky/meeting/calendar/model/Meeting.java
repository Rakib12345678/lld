package com.rocky.meeting.calendar.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a meeting with participants, time slot, reminders, and optional recurrence.
 */
public class Meeting {
    private final String id;
    private String title;
    private String description;
    private TimeSlot timeSlot;
    private MeetingStatus status;
    private List<Participant> participants;
    private List<Reminder> reminders;
    private RecurrencePattern recurrencePattern;

    public Meeting(String title, String description, TimeSlot timeSlot) {
        this.id = UUID.randomUUID().toString();
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.description = description;
        this.timeSlot = Objects.requireNonNull(timeSlot, "TimeSlot must not be null");
        this.status = MeetingStatus.SCHEDULED;
        this.participants = new ArrayList<>();
        this.reminders = new ArrayList<>();
        this.recurrencePattern = null;
    }

    public Meeting(String id, String title, String description, TimeSlot timeSlot,
                   MeetingStatus status, List<Participant> participants,
                   List<Reminder> reminders, RecurrencePattern recurrencePattern) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.description = description;
        this.timeSlot = Objects.requireNonNull(timeSlot, "TimeSlot must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
        this.reminders = reminders != null ? new ArrayList<>(reminders) : new ArrayList<>();
        this.recurrencePattern = recurrencePattern;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public MeetingStatus getStatus() { return status; }
    public List<Participant> getParticipants() { return Collections.unmodifiableList(participants); }
    public List<Reminder> getReminders() { return Collections.unmodifiableList(reminders); }
    public Optional<RecurrencePattern> getRecurrencePattern() { return Optional.ofNullable(recurrencePattern); }
    public boolean isRecurring() { return recurrencePattern != null; }

    // --- Participant management ---

    public void addParticipant(Participant participant) {
        Objects.requireNonNull(participant, "Participant must not be null");
        if (!participants.contains(participant)) {
            participants.add(participant);
        }
    }

    public void addParticipants(List<Participant> newParticipants) {
        Objects.requireNonNull(newParticipants, "Participants list must not be null");
        for (Participant p : newParticipants) {
            addParticipant(p);
        }
    }

    public boolean removeParticipant(String participantId) {
        return participants.removeIf(p -> p.getId().equals(participantId));
    }

    // --- Reminder management ---

    public void addReminder(Reminder reminder) {
        Objects.requireNonNull(reminder, "Reminder must not be null");
        reminders.add(reminder);
    }

    public boolean removeReminder(String reminderId) {
        return reminders.removeIf(r -> r.getId().equals(reminderId));
    }

    // --- Meeting modifications ---

    public void reschedule(TimeSlot newTimeSlot) {
        Objects.requireNonNull(newTimeSlot, "New time slot must not be null");
        this.timeSlot = newTimeSlot;
        this.status = MeetingStatus.RESCHEDULED;
    }

    public void updateDetails(String title, String description) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public void cancel() {
        this.status = MeetingStatus.CANCELLED;
    }

    public void markCompleted() {
        this.status = MeetingStatus.COMPLETED;
    }

    public boolean isActive() {
        return status == MeetingStatus.SCHEDULED || status == MeetingStatus.RESCHEDULED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return Objects.equals(id, meeting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", timeSlot=" + timeSlot +
                ", status=" + status +
                ", participants=" + participants.size() +
                ", recurring=" + isRecurring() +
                '}';
    }
}