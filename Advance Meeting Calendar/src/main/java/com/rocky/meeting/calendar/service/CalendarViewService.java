package com.rocky.meeting.calendar.service;

import com.rocky.meeting.calendar.model.Meeting;
import com.rocky.meeting.calendar.repository.MeetingRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for viewing calendar in different granularities (day/week/month).
 * Separated from MeetingService to follow single responsibility principle.
 */
public class CalendarViewService {
    private final MeetingRepository meetingRepository;

    public CalendarViewService(MeetingRepository meetingRepository) {
        this.meetingRepository = Objects.requireNonNull(meetingRepository, "Repository must not be null");
    }

    /**
     * Returns all meetings scheduled for a specific day.
     */
    public List<Meeting> getDayView(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        return meetingRepository.findByDate(date);
    }

    /**
     * Returns all meetings for a week (Monday to Sunday) containing the given date.
     */
    public List<Meeting> getWeekView(LocalDate dateInWeek) {
        Objects.requireNonNull(dateInWeek, "Date must not be null");

        LocalDate weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        return meetingRepository.findByDateRange(weekStart, weekEnd);
    }

    /**
     * Returns all meetings for a week given a specific start date and end date.
     */
    public List<Meeting> getWeekView(LocalDate weekStart, LocalDate weekEnd) {
        Objects.requireNonNull(weekStart, "Week start must not be null");
        Objects.requireNonNull(weekEnd, "Week end must not be null");

        return meetingRepository.findByDateRange(weekStart, weekEnd);
    }

    /**
     * Returns all meetings for a specific month.
     */
    public List<Meeting> getMonthView(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        return meetingRepository.findByDateRange(monthStart, monthEnd);
    }

    /**
     * Returns meetings organized by day for a given date range.
     * Key = date, Value = list of meetings on that date.
     */
    public Map<LocalDate, List<Meeting>> getMeetingsByDay(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");

        List<Meeting> meetings = meetingRepository.findByDateRange(start, end);

        return meetings.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getTimeSlot().getStartTime().toLocalDate(),
                        TreeMap::new, // sorted by date
                        Collectors.toList()
                ));
    }
}