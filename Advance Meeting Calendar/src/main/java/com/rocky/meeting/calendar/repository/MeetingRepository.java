package com.rocky.meeting.calendar.repository;

import com.rocky.meeting.calendar.model.Meeting;
import com.rocky.meeting.calendar.model.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing meetings.
 * Defines contract for data access operations.
 */
public interface MeetingRepository {
    Meeting save(Meeting meeting);
    Optional<Meeting> findById(String id);
    List<Meeting> findAll();
    List<Meeting> findByDate(LocalDate date);
    List<Meeting> findByDateRange(LocalDate start, LocalDate end);
    List<Meeting> findByTimeSlot(TimeSlot timeSlot);
    List<Meeting> findActiveMeetings();
    boolean deleteById(String id);
    void deleteAll();
    long count();
}