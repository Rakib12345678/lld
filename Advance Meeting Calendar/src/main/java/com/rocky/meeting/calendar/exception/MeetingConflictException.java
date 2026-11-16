package com.rocky.meeting.calendar.exception;

/**
 * Thrown when a meeting time slot conflicts with an existing meeting.
 */
public class MeetingConflictException extends RuntimeException {
    public MeetingConflictException(String message) {
        super(message);
    }

    public MeetingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}