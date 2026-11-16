package com.rocky.meeting.calendar.exception;

/**
 * Thrown when a meeting is not found.
 */
public class MeetingNotFoundException extends RuntimeException {
    public MeetingNotFoundException(String message) {
        super(message);
    }

    public MeetingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}