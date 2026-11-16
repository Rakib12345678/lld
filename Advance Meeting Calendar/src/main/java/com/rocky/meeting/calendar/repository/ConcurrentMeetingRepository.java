package com.rocky.meeting.calendar.repository;

import com.rocky.meeting.calendar.model.Meeting;
import com.rocky.meeting.calendar.model.MeetingStatus;
import com.rocky.meeting.calendar.model.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of MeetingRepository using ReadWriteLock.
 * Supports concurrent read/write operations.
 */
public class ConcurrentMeetingRepository implements MeetingRepository {
    private final Map<String, Meeting> meetings = new ConcurrentHashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true); // fair locking

    @Override
    public Meeting save(Meeting meeting) {
        Objects.requireNonNull(meeting, "Meeting must not be null");
        rwLock.writeLock().lock();
        try {
            meetings.put(meeting.getId(), meeting);
            return meeting;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Meeting> findById(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        rwLock.readLock().lock();
        try {
            return Optional.ofNullable(meetings.get(id));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Meeting> findAll() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(meetings.values());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Meeting> findByDate(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        rwLock.readLock().lock();
        try {
            return meetings.values().stream()
                    .filter(m -> m.isActive())
                    .filter(m -> m.getTimeSlot().getStartTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Meeting> findByDateRange(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date must not be null");
        Objects.requireNonNull(end, "End date must not be null");
        rwLock.readLock().lock();
        try {
            return meetings.values().stream()
                    .filter(m -> m.isActive())
                    .filter(m -> {
                        LocalDate meetingDate = m.getTimeSlot().getStartTime().toLocalDate();
                        return !meetingDate.isBefore(start) && !meetingDate.isAfter(end);
                    })
                    .collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Meeting> findByTimeSlot(TimeSlot timeSlot) {
        Objects.requireNonNull(timeSlot, "TimeSlot must not be null");
        rwLock.readLock().lock();
        try {
            return meetings.values().stream()
                    .filter(m -> m.isActive())
                    .filter(m -> m.getTimeSlot().overlapsWith(timeSlot))
                    .collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Meeting> findActiveMeetings() {
        rwLock.readLock().lock();
        try {
            return meetings.values().stream()
                    .filter(Meeting::isActive)
                    .collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean deleteById(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        rwLock.writeLock().lock();
        try {
            return meetings.remove(id) != null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAll() {
        rwLock.writeLock().lock();
        try {
            meetings.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public long count() {
        rwLock.readLock().lock();
        try {
            return meetings.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }
}