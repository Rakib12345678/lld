package com.rocky.meeting.calendar.service;

import com.rocky.meeting.calendar.exception.MeetingConflictException;
import com.rocky.meeting.calendar.exception.MeetingNotFoundException;
import com.rocky.meeting.calendar.model.*;
import com.rocky.meeting.calendar.repository.MeetingRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service for managing meetings.
 * Handles business logic, conflict detection, and orchestration.
 */
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = Objects.requireNonNull(meetingRepository, "Repository must not be null");
    }

    /**
     * Creates a new meeting and adds participants.
     */
    public Meeting createMeeting(String title, String description, LocalDateTime startTime,
                                  LocalDateTime endTime, List<Participant> participants) {
        return createMeeting(title, description, startTime, endTime, participants, null);
    }

    /**
     * Creates a new meeting with optional recurrence pattern.
     */
    public Meeting createMeeting(String title, String description, LocalDateTime startTime,
                                  LocalDateTime endTime, List<Participant> participants,
                                  RecurrencePattern recurrencePattern) {
        TimeSlot timeSlot = new TimeSlot(startTime, endTime);

        // Check for conflicts
        if (hasConflict(timeSlot)) {
            throw new MeetingConflictException(
                    "Time slot conflict: " + timeSlot + " overlaps with an existing meeting");
        }

        Meeting meeting = new Meeting(title, description, timeSlot);

        if (participants != null) {
            meeting.addParticipants(participants);
        }

        if (recurrencePattern != null) {
            meeting.setRecurrencePattern(recurrencePattern);
        }

        // Auto-add default reminders to every new meeting
        meeting.addReminder(new Reminder(Reminder.ReminderType.EMAIL, Duration.ofMinutes(30)));
        meeting.addReminder(new Reminder(Reminder.ReminderType.NOTIFICATION, Duration.ofMinutes(10)));

        return meetingRepository.save(meeting);
    }

    /**
     * Creates a recurring meeting by generating individual meeting instances
     * based on the recurrence pattern.
     */
    public List<Meeting> createRecurringMeeting(String title, String description,
                                                  LocalDateTime startTime, LocalDateTime endTime,
                                                  List<Participant> participants,
                                                  RecurrencePattern recurrencePattern) {
        Objects.requireNonNull(recurrencePattern, "Recurrence pattern must not be null");

        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        List<LocalDateTime> occurrenceDates = recurrencePattern.generateDates(startTime);

        List<Meeting> createdMeetings = new ArrayList<>();
        List<MeetingConflictException> conflicts = new ArrayList<>();

        for (LocalDateTime occurrenceStart : occurrenceDates) {
            LocalDateTime occurrenceEnd = occurrenceStart.plusMinutes(durationMinutes);
            try {
                Meeting meeting = createMeeting(
                        title + (occurrenceDates.size() > 1 ? " (Recurring #" + (createdMeetings.size() + 1) + ")" : title),
                        description,
                        occurrenceStart,
                        occurrenceEnd,
                        participants,
                        null // recurrence pattern on individual instances
                );
                createdMeetings.add(meeting);
            } catch (MeetingConflictException e) {
                conflicts.add(e);
                // Continue with other occurrences
            }
        }

        return Collections.unmodifiableList(createdMeetings);
    }

    /**
     * Adds participants to an existing meeting.
     */
    public Meeting addParticipants(String meetingId, List<Participant> newParticipants) {
        Meeting meeting = findMeetingOrThrow(meetingId);
        meeting.addParticipants(newParticipants);
        return meetingRepository.save(meeting);
    }

    /**
     * Removes a participant from a meeting.
     */
    public Meeting removeParticipant(String meetingId, String participantId) {
        Meeting meeting = findMeetingOrThrow(meetingId);
        meeting.removeParticipant(participantId);
        return meetingRepository.save(meeting);
    }

    /**
     * Adds a reminder to a meeting.
     */
    public Meeting addReminder(String meetingId, Reminder reminder) {
        Meeting meeting = findMeetingOrThrow(meetingId);
        meeting.addReminder(reminder);
        return meetingRepository.save(meeting);
    }

    /**
     * Reschedules a meeting to a new time slot.
     */
    public Meeting rescheduleMeeting(String meetingId, LocalDateTime newStart, LocalDateTime newEnd) {
        Meeting meeting = findMeetingOrThrow(meetingId);
        TimeSlot newTimeSlot = new TimeSlot(newStart, newEnd);

        // Check for conflicts excluding the current meeting
        List<Meeting> conflictingMeetings = meetingRepository.findByTimeSlot(newTimeSlot)
                .stream()
                .filter(m -> !m.getId().equals(meetingId))
                .collect(Collectors.toList());

        if (!conflictingMeetings.isEmpty()) {
            throw new MeetingConflictException(
                    "Cannot reschedule: " + newTimeSlot + " conflicts with existing meetings");
        }

        meeting.reschedule(newTimeSlot);
        return meetingRepository.save(meeting);
    }

    /**
     * Updates meeting details (title, description).
     */
    public Meeting updateMeetingDetails(String meetingId, String title, String description) {
        Meeting meeting = findMeetingOrThrow(meetingId);
        meeting.updateDetails(title, description);
        return meetingRepository.save(meeting);
    }

    /**
     * Cancels a meeting.
     */
    public Meeting cancelMeeting(String meetingId) {
        Meeting meeting = findMeetingOrThrow(meetingId);
        meeting.cancel();
        return meetingRepository.save(meeting);
    }

    /**
     * Gets a meeting by ID.
     */
    public Meeting getMeeting(String meetingId) {
        return findMeetingOrThrow(meetingId);
    }

    /**
     * Gets all meetings.
     */
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    /**
     * Deletes a meeting permanently.
     */
    public boolean deleteMeeting(String meetingId) {
        if (meetingRepository.findById(meetingId).isEmpty()) {
            throw new MeetingNotFoundException("Meeting not found with ID: " + meetingId);
        }
        return meetingRepository.deleteById(meetingId);
    }

    /**
     * Checks if a time slot has any conflicting meetings.
     */
    public boolean hasConflict(TimeSlot timeSlot) {
        return !meetingRepository.findByTimeSlot(timeSlot).isEmpty();
    }

    /**
     * Finds meetings that overlap with a given time slot.
     */
    public List<Meeting> findConflictingMeetings(TimeSlot timeSlot) {
        return meetingRepository.findByTimeSlot(timeSlot);
    }

    private Meeting findMeetingOrThrow(String meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting not found with ID: " + meetingId));
    }
}