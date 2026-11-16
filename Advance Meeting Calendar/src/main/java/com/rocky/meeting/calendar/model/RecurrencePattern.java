package com.rocky.meeting.calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Defines a recurrence pattern for recurring meetings.
 * Simplified: only frequency and end date, no interval or occurrences.
 */
public final class RecurrencePattern {
    public enum Frequency {
        DAILY,
        WEEKLY,
        BI_WEEKLY,
        MONTHLY
    }

    private final Frequency frequency;
    private final LocalDate endDate;

    public RecurrencePattern(Frequency frequency, LocalDate endDate) {
        this.frequency = Objects.requireNonNull(frequency, "Frequency must not be null");
        this.endDate = Objects.requireNonNull(endDate, "End date must not be null");
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Generates all meeting start times based on the recurrence pattern
     * starting from the given start time, until the end date.
     */
    public List<LocalDateTime> generateDates(LocalDateTime startTime) {
        List<LocalDateTime> dates = new ArrayList<>();
        LocalDateTime current = startTime;

        while (true) {
            dates.add(current);

            // Compute next date based on frequency
            LocalDateTime next;
            switch (frequency) {
                case DAILY:
                    next = current.plusDays(1);
                    break;
                case WEEKLY:
                    next = current.plusWeeks(1);
                    break;
                case BI_WEEKLY:
                    next = current.plusWeeks(2);
                    break;
                case MONTHLY:
                    next = current.plusMonths(1);
                    break;
                default:
                    throw new IllegalStateException("Unknown frequency: " + frequency);
            }

            // Stop if next occurrence would be after end date
            if (next.toLocalDate().isAfter(endDate)) {
                break;
            }

            current = next;
        }

        return Collections.unmodifiableList(dates);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecurrencePattern that = (RecurrencePattern) o;
        return frequency == that.frequency &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequency, endDate);
    }

    @Override
    public String toString() {
        return "RecurrencePattern{" +
                "frequency=" + frequency +
                ", endDate=" + endDate +
                '}';
    }
}