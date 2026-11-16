package com.rocky.meeting.calendar.service;

import com.rocky.meeting.calendar.model.Meeting;
import com.rocky.meeting.calendar.model.Participant;
import com.rocky.meeting.calendar.model.Reminder;
import com.rocky.meeting.calendar.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background scheduler that continuously checks for due reminders
 * and sends notifications to participants.
 * Runs at a configurable interval (default 5 seconds) using ScheduledExecutorService.
 *
 * The scheduler dynamically calculates due reminders at runtime:
 * - Checks if meeting is active (not cancelled/completed)
 * - Checks if trigger time has passed
 * - Checks if meeting hasn't started yet
 * - Tracks already-sent reminders in memory to avoid duplicates
 */
public class ReminderScheduler {

    private final ReminderService reminderService;
    private final MeetingRepository meetingRepository;
    private final long checkIntervalSeconds;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<String> sentReminders = new HashSet<>();

    public ReminderScheduler(ReminderService reminderService, MeetingRepository meetingRepository) {
        this(reminderService, meetingRepository, 5);
    }

    public ReminderScheduler(ReminderService reminderService, MeetingRepository meetingRepository,
                             long checkIntervalSeconds) {
        this.reminderService = Objects.requireNonNull(reminderService, "ReminderService must not be null");
        this.meetingRepository = Objects.requireNonNull(meetingRepository, "MeetingRepository must not be null");
        this.checkIntervalSeconds = checkIntervalSeconds > 0 ? checkIntervalSeconds : 5;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Starts the background reminder scheduler.
     * It will check for due reminders every {@code checkIntervalSeconds}.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            System.out.println("[ReminderScheduler] Started — checking every " + checkIntervalSeconds + " seconds");
            scheduler.scheduleAtFixedRate(
                    this::checkAndSendReminders,
                    0,
                    checkIntervalSeconds,
                    TimeUnit.SECONDS
            );
        }
    }

    /**
     * Stops the background scheduler gracefully.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            System.out.println("[ReminderScheduler] Stopping...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[ReminderScheduler] Stopped");
        }
    }

    /**
     * Checks for due reminders and sends notifications.
     * Called periodically by the scheduler thread.
     * Dynamically calculates based on current meeting state and time.
     */
    private void checkAndSendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<ReminderService.ReminderDue> dueReminders = reminderService.getDueReminders(now);

            for (ReminderService.ReminderDue due : dueReminders) {
                String key = due.getMeeting().getId() + ":" + due.getReminder().getId();

                // Only send if we haven't already sent this reminder
                if (!sentReminders.contains(key)) {
                    sendReminderNotification(due.getMeeting(), due.getReminder());
                    sentReminders.add(key);
                }
            }

        } catch (Exception e) {
            System.err.println("[ReminderScheduler] Error checking reminders: " + e.getMessage());
        }
    }

    /**
     * Sends a reminder notification to all participants of the meeting.
     * In a real system, this would integrate with email/SMS/push notification services.
     */
    private void sendReminderNotification(Meeting meeting, Reminder reminder) {
        String meetingTitle = meeting.getTitle();
        String startTime = meeting.getTimeSlot().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String endTime = meeting.getTimeSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String reminderType = reminder.getType().name();

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  [REMINDER] " + reminderType + " Notification");
        System.out.println("=".repeat(60));
        System.out.println("  Meeting  : " + meetingTitle);
        System.out.println("  Time     : " + startTime + " - " + endTime);
        System.out.println("  Reminder : " + reminder.getTimeBeforeMeeting().toMinutes() + " minutes before");
        System.out.println("  Sending to:");

        for (Participant participant : meeting.getParticipants()) {
            switch (reminder.getType()) {
                case EMAIL:
                    System.out.println("    📧 Email sent to " + participant.getName()
                            + " <" + participant.getEmail() + ">");
                    break;
                case NOTIFICATION:
                    System.out.println("    🔔 Push notification sent to " + participant.getName()
                            + " (device: " + participant.getEmail() + ")");
                    break;
                case SOUND:
                    System.out.println("    🔊 Sound alert sent to " + participant.getName());
                    break;
            }
        }
        System.out.println("=".repeat(60));
        System.out.println();
    }

    /**
     * Returns whether the scheduler is currently running.
     */
    public boolean isRunning() {
        return running.get();
    }
}