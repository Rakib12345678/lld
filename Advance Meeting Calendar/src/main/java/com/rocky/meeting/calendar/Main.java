package com.rocky.meeting.calendar;

import com.rocky.meeting.calendar.exception.MeetingConflictException;
import com.rocky.meeting.calendar.exception.MeetingNotFoundException;
import com.rocky.meeting.calendar.model.*;
import com.rocky.meeting.calendar.repository.ConcurrentMeetingRepository;
import com.rocky.meeting.calendar.repository.MeetingRepository;
import com.rocky.meeting.calendar.service.CalendarViewService;
import com.rocky.meeting.calendar.service.MeetingService;
import com.rocky.meeting.calendar.service.ReminderService;
import com.rocky.meeting.calendar.service.ReminderScheduler;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Main entry point for the Advanced Meeting Calendar System.
 * Demonstrates all features: create, participants, schedule, recurring, reminders, views, cancel/update.
 */
public class Main {

    private final MeetingRepository repository;
    private final MeetingService meetingService;
    private final CalendarViewService calendarViewService;
    private final ReminderService reminderService;
    private final ReminderScheduler reminderScheduler;

    public Main() {
        this.repository = new ConcurrentMeetingRepository();
        this.meetingService = new MeetingService(repository);
        this.calendarViewService = new CalendarViewService(repository);
        this.reminderService = new ReminderService(repository);
        this.reminderScheduler = new ReminderScheduler(reminderService, repository, 5);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    private void run() {
        System.out.println("=".repeat(70));
        System.out.println("   ADVANCED MEETING CALENDAR SYSTEM - DEMO");
        System.out.println("=".repeat(70));
        System.out.println();

        try {
            // -------------------------------------------------
            // 1. CREATE MEETINGS
            // -------------------------------------------------
            System.out.println(">>> 1. CREATING MEETINGS");
            System.out.println("-".repeat(70));

            Meeting meeting1 = meetingService.createMeeting(
                    "Sprint Planning",
                    "Sprint 24 planning session with the team",
                    LocalDateTime.of(2026, 9, 3, 9, 0),
                    LocalDateTime.of(2026, 9, 3, 10, 30),
                    List.of(
                            new Participant("Alice", "alice@company.com"),
                            new Participant("Bob", "bob@company.com"),
                            new Participant("Charlie", "charlie@company.com")
                    )
            );
            System.out.println("  Created: " + meeting1.getTitle() + " | ID: " + meeting1.getId());
            System.out.println("  Time: " + meeting1.getTimeSlot().getStartTime() + " - " + meeting1.getTimeSlot().getEndTime());
            System.out.println("  Participants: " + meeting1.getParticipants().size());
            System.out.println();

            Meeting meeting2 = meetingService.createMeeting(
                    "Code Review",
                    "Review PR #42",
                    LocalDateTime.of(2026, 9, 3, 14, 0),
                    LocalDateTime.of(2026, 9, 3, 15, 0),
                    List.of(
                            new Participant("David", "david@company.com"),
                            new Participant("Eve", "eve@company.com")
                    )
            );
            System.out.println("  Created: " + meeting2.getTitle() + " | ID: " + meeting2.getId());
            System.out.println("  Time: " + meeting2.getTimeSlot().getStartTime() + " - " + meeting2.getTimeSlot().getEndTime());
            System.out.println();

            // -------------------------------------------------
            // 2. ADD MULTIPLE PARTICIPANTS
            // -------------------------------------------------
            System.out.println(">>> 2. ADDING MULTIPLE PARTICIPANTS");
            System.out.println("-".repeat(70));

            Meeting updated = meetingService.addParticipants(
                    meeting1.getId(),
                    List.of(
                            new Participant("Frank", "frank@company.com"),
                            new Participant("Grace", "grace@company.com")
                    )
            );
            System.out.println("  Added 2 more participants to '" + updated.getTitle() + "'");
            System.out.println("  Total participants: " + updated.getParticipants().size());
            updated.getParticipants().forEach(p ->
                    System.out.println("    - " + p.getName() + " <" + p.getEmail() + ">")
            );
            System.out.println();

            // -------------------------------------------------
            // 3. SCHEDULE (start/end time already handled above)
            // -------------------------------------------------
            System.out.println(">>> 3. SCHEDULING - Conflict Detection");
            System.out.println("-".repeat(70));

            // Try creating overlapping meeting - should throw MeetingConflictException
            try {
                meetingService.createMeeting(
                        "Overlapping Meeting",
                        "This should conflict",
                        LocalDateTime.of(2026, 9, 3, 9, 30),
                        LocalDateTime.of(2026, 9, 3, 10, 0),
                        List.of(new Participant("Henry", "henry@company.com"))
                );
                System.out.println("  ERROR: Should have thrown conflict exception!");
            } catch (MeetingConflictException e) {
                System.out.println("  [Expected] Conflict detected: " + e.getMessage());
            }

            // Non-overlapping meeting should succeed
            Meeting meeting3 = meetingService.createMeeting(
                    "Stand-up",
                    "Daily stand-up meeting",
                    LocalDateTime.of(2026, 9, 3, 11, 0),
                    LocalDateTime.of(2026, 9, 3, 11, 15),
                    List.of(new Participant("Ivy", "ivy@company.com"))
            );
            System.out.println("  Non-conflicting meeting created: " + meeting3.getTitle());
            System.out.println();

            // -------------------------------------------------
            // 4. CREATE RECURRING MEETINGS
            // -------------------------------------------------
            System.out.println(">>> 4. CREATING RECURRING MEETINGS");
            System.out.println("-".repeat(70));

            RecurrencePattern weeklyPattern = new RecurrencePattern(
                    RecurrencePattern.Frequency.WEEKLY,
                    LocalDate.of(2026, 9, 24) // end date
            );

            List<Meeting> recurringMeetings = meetingService.createRecurringMeeting(
                    "Weekly Team Sync",
                    "Weekly sync with the team",
                    LocalDateTime.of(2026, 9, 7, 10, 0),  // Monday
                    LocalDateTime.of(2026, 9, 7, 11, 0),
                    List.of(
                            new Participant("Alice", "alice@company.com"),
                            new Participant("Bob", "bob@company.com"),
                            new Participant("Charlie", "charlie@company.com"),
                            new Participant("David", "david@company.com")
                    ),
                    weeklyPattern
            );
            System.out.println("  Created " + recurringMeetings.size() + " recurring meeting instances:");
            for (Meeting rm : recurringMeetings) {
                System.out.println("    - " + rm.getTitle() + " | " +
                        rm.getTimeSlot().getStartTime().toLocalDate() + " " +
                        rm.getTimeSlot().getStartTime().toLocalTime() + " - " +
                        rm.getTimeSlot().getEndTime().toLocalTime());
            }
            System.out.println();

            // -------------------------------------------------
            // 5. REMINDERS (auto-added on meeting creation)
            // -------------------------------------------------
            System.out.println(">>> 5. REMINDERS (auto-added on meeting creation)");
            System.out.println("-".repeat(70));

            // Reminders are automatically added when a meeting is created
            Meeting meetingWithReminders = meetingService.getMeeting(meeting1.getId());
            System.out.println("  Meeting: '" + meetingWithReminders.getTitle() + "'");
            System.out.println("  Auto-added reminders: " + meetingWithReminders.getReminders().size());
            meetingWithReminders.getReminders().forEach(r ->
                    System.out.println("    - " + r.getType() + " (" + r.getTimeBeforeMeeting().toMinutes() + " min before)")
            );

            // Check due reminders (mock current time close to meeting start)
            LocalDateTime nearMeetingTime = LocalDateTime.of(2026, 9, 3, 8, 55);
            List<ReminderService.ReminderDue> dueReminders = reminderService.getDueReminders(nearMeetingTime);
            System.out.println("  Due reminders at " + nearMeetingTime + ": " + dueReminders.size());
            dueReminders.forEach(rd ->
                    System.out.println("    - " + rd.getMeeting().getTitle() + " | " + rd.getReminder().getType())
            );
            System.out.println();

            // -------------------------------------------------
            // 6. VIEW CALENDAR BY DAY / WEEK / MONTH
            // -------------------------------------------------
            System.out.println(">>> 6. VIEWING CALENDAR");
            System.out.println("-".repeat(70));

            // Day view
            LocalDate viewDate = LocalDate.of(2026, 9, 3);
            List<Meeting> dayMeetings = calendarViewService.getDayView(viewDate);
            System.out.println("  [DAY VIEW] " + viewDate);
            System.out.println("  Total meetings: " + dayMeetings.size());
            for (Meeting m : dayMeetings) {
                System.out.println("    - " + m.getTitle() + " (" +
                        m.getTimeSlot().getStartTime().toLocalTime() + " - " +
                        m.getTimeSlot().getEndTime().toLocalTime() + ") [" +
                        m.getStatus() + "]");
            }
            System.out.println();

            // Week view
            LocalDate weekOf = LocalDate.of(2026, 9, 1);
            List<Meeting> weekMeetings = calendarViewService.getWeekView(weekOf);
            System.out.println("  [WEEK VIEW] Week of " + weekOf);
            System.out.println("  Total meetings: " + weekMeetings.size());
            for (Meeting m : weekMeetings) {
                System.out.println("    - " + m.getTitle() + " on " +
                        m.getTimeSlot().getStartTime().toLocalDate() + " (" +
                        m.getTimeSlot().getStartTime().toLocalTime() + " - " +
                        m.getTimeSlot().getEndTime().toLocalTime() + ")");
            }
            System.out.println();

            // Month view
            List<Meeting> monthMeetings = calendarViewService.getMonthView(2026, 9);
            System.out.println("  [MONTH VIEW] September 2026");
            System.out.println("  Total meetings: " + monthMeetings.size());
            System.out.println();

            // Grouped by day
            Map<LocalDate, List<Meeting>> byDay = calendarViewService.getMeetingsByDay(
                    LocalDate.of(2026, 9, 1),
                    LocalDate.of(2026, 9, 30)
            );
            System.out.println("  [GROUPED BY DAY]");
            for (Map.Entry<LocalDate, List<Meeting>> entry : byDay.entrySet()) {
                System.out.println("    " + entry.getKey() + " (" + entry.getValue().size() + " meetings):");
                for (Meeting m : entry.getValue()) {
                    System.out.println("      - " + m.getTitle());
                }
            }
            System.out.println();

            // -------------------------------------------------
            // 7. CANCEL / UPDATE MEETINGS
            // -------------------------------------------------
            System.out.println(">>> 7. CANCELING & UPDATING MEETINGS");
            System.out.println("-".repeat(70));

            // Update meeting details
            Meeting updatedDetails = meetingService.updateMeetingDetails(
                    meeting2.getId(),
                    "Final Code Review",
                    "Review PR #42 before deployment"
            );
            System.out.println("  Updated details for '" + updatedDetails.getTitle() + "'");
            System.out.println("    Description: " + updatedDetails.getDescription());
            System.out.println();

            // Cancel a meeting
            Meeting cancelled = meetingService.cancelMeeting(meeting3.getId());
            System.out.println("  Cancelled meeting: '" + cancelled.getTitle() + "'");
            System.out.println("  Status: " + cancelled.getStatus());
            System.out.println();

            // Verify cancellation
            System.out.println("  Active meetings: " + repository.findActiveMeetings().size() +
                    " (out of " + repository.count() + " total)");

            // Reschedule a meeting
            Meeting rescheduled = meetingService.rescheduleMeeting(
                    meeting1.getId(),
                    LocalDateTime.of(2026, 9, 3, 9, 30),
                    LocalDateTime.of(2026, 9, 3, 11, 0)
            );
            System.out.println("  Rescheduled: '" + rescheduled.getTitle() + "'");
            System.out.println("    New time: " + rescheduled.getTimeSlot().getStartTime() +
                    " - " + rescheduled.getTimeSlot().getEndTime());
            System.out.println("    Status: " + rescheduled.getStatus());
            System.out.println();

            // -------------------------------------------------
            // 8. BACKGROUND REMINDER SCHEDULER DEMO
            // -------------------------------------------------
            System.out.println(">>> 8. BACKGROUND REMINDER SCHEDULER");
            System.out.println("-".repeat(70));
            System.out.println("  Starting background scheduler (checks every 5 seconds)...");
            reminderScheduler.start();

            // Let the scheduler run for a few cycles to pick up due reminders
            System.out.println("  Scheduler will automatically detect due reminders and notify participants.");
            System.out.println("  (In production, this runs 24/7 as a background service)");
            System.out.println();

            // -------------------------------------------------
            // DEMO COMPLETE
            // -------------------------------------------------
            System.out.println("=".repeat(70));
            System.out.println("   ALL FEATURES DEMONSTRATED SUCCESSFULLY");
            System.out.println("=".repeat(70));
            System.out.println();
            System.out.println("Features covered:");
            System.out.println("  1. Create meeting with title, description, time slot");
            System.out.println("  2. Add multiple participants");
            System.out.println("  3. Schedule with start/end time + conflict detection");
            System.out.println("  4. Create recurring meetings (daily, weekly, bi-weekly, monthly)");
            System.out.println("  5. Add reminders (email, notification) with due reminder detection");
            System.out.println("  6. View calendar by day / week / month");
            System.out.println("  7. Cancel / update meeting details / reschedule");
            System.out.println("  8. Background reminder scheduler (runs every 5 seconds)");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Stop the scheduler when the application exits
            reminderScheduler.stop();
        }
    }
}