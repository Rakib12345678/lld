package com.rocky.meeting.calendar.service;

import com.rocky.meeting.calendar.model.Meeting;
import com.rocky.meeting.calendar.model.Reminder;
import com.rocky.meeting.calendar.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing meeting reminders.
 * Separated to follow single responsibility principle.
 */
public class ReminderService {
    private final MeetingRepository meetingRepository;

    public ReminderService(MeetingRepository meetingRepository) {
        this.meetingRepository = Objects.requireNonNull(meetingRepository, "Repository must not be null");
    }

    /**
     * Adds a reminder to a meeting.
     */
    public Meeting addReminder(String meetingId, Reminder.ReminderType type,
                                java.time.Duration timeBeforeMeeting) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new com.rocky.meeting.calendar.exception.MeetingNotFoundException(
                        "Meeting not found with ID: " + meetingId));

        Reminder reminder = new Reminder(type, timeBeforeMeeting);
        meeting.addReminder(reminder);
        return meetingRepository.save(meeting);
    }

    /**
     * Removes a reminder from a meeting.
     */
    public Meeting removeReminder(String meetingId, String reminderId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new com.rocky.meeting.calendar.exception.MeetingNotFoundException(
                        "Meeting not found with ID: " + meetingId));

        meeting.removeReminder(reminderId);
        return meetingRepository.save(meeting);
    }

    /**
     * Returns all reminders that are due to be sent at the given time.
     * A reminder is "due" if:
     * 1. The meeting is active (SCHEDULED or RESCHEDULED)
     * 2. The trigger time has passed (now >= triggerTime)
     * 3. The meeting hasn't started yet (now < meetingStart)
     */
    public List<ReminderDue> getDueReminders(LocalDateTime now) {
        Objects.requireNonNull(now, "Current time must not be null");

        List<ReminderDue> dueReminders = new ArrayList<>();

        for (Meeting meeting : meetingRepository.findActiveMeetings()) {
            LocalDateTime meetingStart = meeting.getTimeSlot().getStartTime();

            // Meeting hasn't started yet
            if (!now.isBefore(meetingStart)) {
                continue;
            }

            for (Reminder reminder : meeting.getReminders()) {
                LocalDateTime triggerTime = reminder.getTriggerTime(meetingStart);
                // Trigger time has passed (or is now)
                if (!triggerTime.isAfter(now)) {
                    dueReminders.add(new ReminderDue(meeting, reminder));
                }
            }
        }

        return Collections.unmodifiableList(dueReminders);
    }

    /**
     * Value object holding a meeting and its due reminder.
     */
    public static class ReminderDue {
        private final Meeting meeting;
        private final Reminder reminder;

        public ReminderDue(Meeting meeting, Reminder reminder) {
            this.meeting = meeting;
            this.reminder = reminder;
        }

        public Meeting getMeeting() {
            return meeting;
        }

        public Reminder getReminder() {
            return reminder;
        }

        @Override
        public String toString() {
            return "ReminderDue{" +
                    "meeting='" + meeting.getTitle() + '\'' +
                    ", reminder=" + reminder +
                    '}';
        }
    }
}