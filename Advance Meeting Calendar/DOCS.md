# Advanced Meeting Calendar — Low-Level Design Document

## 1. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                          │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Main (Entry Point)                        │   │
│  │  - Orchestrates demo flow                                    │   │
│  │  - Creates services, executes all 7 features                 │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         SERVICE LAYER                               │
│                                                                     │
│  ┌───────────────────┐  ┌───────────────────┐  ┌───────────────┐   │
│  │   MeetingService   │  │ CalendarViewService│  │ ReminderService│   │
│  │   - Business logic │  │ - Day/Week/Month  │  │ - Reminder    │   │
│  │   - Conflict       │  │ - Group by day    │  │   management  │   │
│  │     detection      │  │                   │  │ - Due reminder│   │
│  │   - CRUD ops       │  │                   │  │   detection   │   │
│  └────────┬───────────┘  └────────┬──────────┘  └───────┬───────┘   │
└───────────┼────────────────────────┼──────────────────────┼─────────┘
            │                        │                      │
            ▼                        ▼                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       REPOSITORY LAYER                              │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                   MeetingRepository (Interface)               │   │
│  │  + save(Meeting) : Meeting                                   │   │
│  │  + findById(String) : Optional<Meeting>                      │   │
│  │  + findByDate(LocalDate) : List<Meeting>                     │   │
│  │  + findByTimeSlot(TimeSlot) : List<Meeting>                  │   │
│  │  + findByDateRange(LocalDate, LocalDate) : List<Meeting>     │   │
│  │  + findActiveMeetings() : List<Meeting>                      │   │
│  │  + findAll() : List<Meeting>                                 │   │
│  │  + deleteById(String) : boolean                              │   │
│  │  + deleteAll() : void                                        │   │
│  │  + count() : long                                            │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
│                         │ implements                                │
│  ┌──────────────────────┴───────────────────────────────────────┐   │
│  │             ConcurrentMeetingRepository                       │   │
│  │  - Thread-safe in-memory implementation                      │   │
│  │  - Uses ConcurrentHashMap + ReadWriteLock (fair locking)     │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER (Model)                        │
│                                                                     │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌─────────────────┐   │
│  │  Meeting │──│ TimeSlot  │  │Reminder  │  │RecurrencePattern│   │
│  ├──────────┤  ├───────────┤  ├──────────┤  ├─────────────────┤   │
│  │- id      │  │- start    │  │- id      │  │- frequency      │   │
│  │- title   │  │- end      │  │- type    │  │- interval       │   │
│  │- desc    │  └───────────┘  │- timeBefore│  │- endDate       │   │
│  │- status──┼───┐            │- sent    │  │- occurrences    │   │
│  │- ts──────┘   │            └──────────┘  └─────────────────┘   │   │
│  │- part.───┼───┤                                                 │   │
│  │- reminders │ │  ┌───────────────┐                              │   │
│  │- recurr │   │  │   Participant  │                              │   │
│  └──────────┘   │  ├───────────────┤                              │   │
│                 └──│- id           │                              │   │
│                    │- name         │                              │   │
│                    │- email        │                              │   │
│                    └───────────────┘                              │   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Complete Class Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        <<enumeration>>                              │
│                        MeetingStatus                                │
├─────────────────────────────────────────────────────────────────────┤
│  SCHEDULED                                                          │
│  CANCELLED                                                          │
│  COMPLETED                                                          │
│  RESCHEDULED                                                        │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     <<value object>>                                │
│                        TimeSlot                                     │
├─────────────────────────────────────────────────────────────────────┤
│  - startTime : LocalDateTime (final)                                │
│  - endTime : LocalDateTime (final)                                  │
├─────────────────────────────────────────────────────────────────────┤
│  + TimeSlot(startTime, endTime)                                     │
│  + getStartTime() : LocalDateTime                                   │
│  + getEndTime() : LocalDateTime                                     │
│  + overlapsWith(other : TimeSlot) : boolean                         │
│  + contains(dateTime : LocalDateTime) : boolean                     │
│  + equals() / hashCode() / toString()                               │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     <<value object>>                                │
│                        Participant                                  │
├─────────────────────────────────────────────────────────────────────┤
│  - id : String (final)                                              │
│  - name : String (final)                                            │
│  - email : String (final)                                           │
├─────────────────────────────────────────────────────────────────────┤
│  + Participant(name, email)                                         │
│  + Participant(id, name, email)                                     │
│  + getId() / getName() / getEmail()                                 │
│  + equals() / hashCode() / toString()                               │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     <<value object>>                                │
│                        Reminder                                     │
├─────────────────────────────────────────────────────────────────────┤
│  - id : String (final)                                              │
│  - type : ReminderType (final)                                      │
│  - timeBeforeMeeting : Duration (final)                             │
│  - sent : boolean                                                   │
├─────────────────────────────────────────────────────────────────────┤
│  + Reminder(type, timeBeforeMeeting)                                │
│  + Reminder(id, type, timeBeforeMeeting, sent)                      │
│  + getId() / getType() / getTimeBeforeMeeting() / isSent()          │
│  + markAsSent() : void                                              │
│  + getTriggerTime(meetingStart) : LocalDateTime                     │
│  + equals() / hashCode() / toString()                               │
└─────────────────────────────────────────────────────────────────────┘
         ▲
         │ contains
         │
┌─────────────────────────────────────────────────────────────────────┐
│                    <<enumeration>>                                  │
│                    Reminder.ReminderType                            │
├─────────────────────────────────────────────────────────────────────┤
│  EMAIL                                                              │
│  NOTIFICATION                                                       │
│  SOUND                                                              │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     <<value object>>                                │
│                     RecurrencePattern                               │
├─────────────────────────────────────────────────────────────────────┤
│  - frequency : Frequency (final)                                    │
│  - interval : int (final)                                           │
│  - endDate : LocalDate (final, optional)                            │
│  - occurrences : Integer (final, optional)                          │
├─────────────────────────────────────────────────────────────────────┤
│  + RecurrencePattern(freq, interval, endDate)                       │
│  + RecurrencePattern(freq, interval, occurrences)                   │
│  + generateDates(startTime) : List<LocalDateTime>                   │
│  + getFrequency() / getInterval() / getEndDate() / getOccurrences() │
└─────────────────────────────────────────────────────────────────────┘
         ▲
         │ <<enumeration>>
         │ RecurrencePattern.Frequency
         ├─────────────────────────
         │  DAILY
         │  WEEKLY
         │  BI_WEEKLY
         │  MONTHLY
         └─────────────────────────

┌─────────────────────────────────────────────────────────────────────┐
│                      <<entity>>                                     │
│                        Meeting                                      │
├─────────────────────────────────────────────────────────────────────┤
│  - id : String (final)                                              │
│  - title : String                                                   │
│  - description : String                                             │
│  - timeSlot : TimeSlot                                              │
│  - status : MeetingStatus                                           │
│  - participants : List<Participant>                                 │
│  - reminders : List<Reminder>                                       │
│  - recurrencePattern : RecurrencePattern (optional)                 │
├─────────────────────────────────────────────────────────────────────┤
│  + Meeting(title, desc, timeSlot)                                   │
│  + Meeting(id, title, desc, timeSlot, status, parts, rems, recur)  │
│  + getId() / getTitle() / getDescription() / getTimeSlot()          │
│  + getStatus() / getParticipants() / getReminders()                 │
│  + getRecurrencePattern() : Optional                                │
│  + isRecurring() : boolean                                          │
│  + addParticipant(Participant) : void                               │
│  + addParticipants(List<Participant>) : void                        │
│  + removeParticipant(String) : boolean                              │
│  + addReminder(Reminder) : void                                     │
│  + removeReminder(String) : boolean                                 │
│  + reschedule(TimeSlot) : void                                      │
│  + updateDetails(title, desc) : void                                │
│  + setRecurrencePattern(RecurrencePattern) : void                   │
│  + cancel() : void                                                  │
│  + markCompleted() : void                                           │
│  + isActive() : boolean                                             │
│  + equals() / hashCode() / toString()                               │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    <<interface>>                                    │
│                    MeetingRepository                                │
├─────────────────────────────────────────────────────────────────────┤
│  + save(Meeting) : Meeting                                          │
│  + findById(String) : Optional<Meeting>                             │
│  + findAll() : List<Meeting>                                        │
│  + findByDate(LocalDate) : List<Meeting>                            │
│  + findByDateRange(LocalDate, LocalDate) : List<Meeting>            │
│  + findByTimeSlot(TimeSlot) : List<Meeting>                         │
│  + findActiveMeetings() : List<Meeting>                             │
│  + deleteById(String) : boolean                                     │
│  + deleteAll() : void                                               │
│  + count() : long                                                   │
└─────────────────────────────────────────────────────────────────────┘
         ▲ implements
         │
┌─────────────────────────────────────────────────────────────────────┐
│               ConcurrentMeetingRepository                           │
├─────────────────────────────────────────────────────────────────────┤
│  - meetings : Map<String, Meeting> = ConcurrentHashMap<>()          │
│  - rwLock : ReadWriteLock = ReentrantReadWriteLock(true)            │
│  (all methods are thread-safe via read/write locks)                  │
├─────────────────────────────────────────────────────────────────────┤
│  Implements all MeetingRepository methods                           │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                        MeetingService                               │
├─────────────────────────────────────────────────────────────────────┤
│  - meetingRepository : MeetingRepository (final)                    │
├─────────────────────────────────────────────────────────────────────┤
│  + MeetingService(MeetingRepository)                                │
│  + createMeeting(title, desc, start, end, participants) : Meeting   │
│  + createMeeting(title, desc, start, end, participants, pattern)    │
│  + createRecurringMeeting(...) : List<Meeting>                      │
│  + addParticipants(meetingId, participants) : Meeting               │
│  + removeParticipant(meetingId, participantId) : Meeting            │
│  + addReminder(meetingId, reminder) : Meeting                       │
│  + rescheduleMeeting(meetingId, newStart, newEnd) : Meeting         │
│  + updateMeetingDetails(meetingId, title, desc) : Meeting           │
│  + cancelMeeting(meetingId) : Meeting                               │
│  + getMeeting(meetingId) : Meeting                                  │
│  + getAllMeetings() : List<Meeting>                                 │
│  + deleteMeeting(meetingId) : boolean                               │
│  + hasConflict(TimeSlot) : boolean                                  │
│  + findConflictingMeetings(TimeSlot) : List<Meeting>                │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     CalendarViewService                             │
├─────────────────────────────────────────────────────────────────────┤
│  - meetingRepository : MeetingRepository (final)                    │
├─────────────────────────────────────────────────────────────────────┤
│  + CalendarViewService(MeetingRepository)                           │
│  + getDayView(LocalDate) : List<Meeting>                            │
│  + getWeekView(LocalDate) : List<Meeting>                           │
│  + getWeekView(start, end) : List<Meeting>                          │
│  + getMonthView(year, month) : List<Meeting>                        │
│  + getMeetingsByDay(start, end) : Map<LocalDate, List<Meeting>>    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                       ReminderService                               │
├─────────────────────────────────────────────────────────────────────┤
│  - meetingRepository : MeetingRepository (final)                    │
├─────────────────────────────────────────────────────────────────────┤
│  + ReminderService(MeetingRepository)                               │
│  + addReminder(meetingId, type, timeBefore) : Meeting               │
│  + removeReminder(meetingId, reminderId) : Meeting                  │
│  + getDueReminders(LocalDateTime) : List<ReminderDue>               │
│  + markRemindersSent(meetingId) : Meeting                           │
├─────────────────────────────────────────────────────────────────────┤
│  + <<static>> class ReminderDue                                     │
│    - meeting : Meeting                                              │
│    - reminder : Reminder                                            │
│    + ReminderDue(Meeting, Reminder)                                 │
│    + getMeeting() / getReminder() / toString()                      │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    <<exception>>                                    │
│                    MeetingConflictException                         │
├─────────────────────────────────────────────────────────────────────┤
│  extends RuntimeException                                          │
│  + MeetingConflictException(String)                                 │
│  + MeetingConflictException(String, Throwable)                      │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    <<exception>>                                    │
│                    MeetingNotFoundException                         │
├─────────────────────────────────────────────────────────────────────┤
│  extends RuntimeException                                          │
│  + MeetingNotFoundException(String)                                 │
│  + MeetingNotFoundException(String, Throwable)                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Relationship Matrix

| Relationship | Type | Direction | Description |
|---|---|---|---|
| **Meeting → TimeSlot** | Composition | 1 → 1 | A Meeting **has a** TimeSlot (start/end). When Meeting is destroyed, its TimeSlot is destroyed. |
| **Meeting → Participant** | Aggregation | 1 → 0..* | A Meeting **has many** Participants. Participants can exist independently. |
| **Meeting → Reminder** | Composition | 1 → 0..* | A Meeting **has many** Reminders. Reminders belong to exactly one Meeting. |
| **Meeting → RecurrencePattern** | Association | 1 → 0..1 | A Meeting **may have** a RecurrencePattern. Optional relationship. |
| **Meeting → MeetingStatus** | Association | 1 → 1 | A Meeting **has a** Status (enum). |
| **MeetingService → MeetingRepository** | Dependency | 1 → 1 | MeetingService **depends on** MeetingRepository interface. |
| **CalendarViewService → MeetingRepository** | Dependency | 1 → 1 | CalendarViewService **depends on** MeetingRepository interface. |
| **ReminderService → MeetingRepository** | Dependency | 1 → 1 | ReminderService **depends on** MeetingRepository interface. |
| **ConcurrentMeetingRepository → MeetingRepository** | Realization | 1 → 1 | ConcurrentMeetingRepository **implements** MeetingRepository interface. |
| **Reminder → Reminder.ReminderType** | Association | 1 → 1 | Each Reminder has a ReminderType (enum). |
| **RecurrencePattern → RecurrencePattern.Frequency** | Association | 1 → 1 | Each pattern has a Frequency (enum). |
| **MeetingService → MeetingConflictException** | Dependency | → | Thrown when time slot conflicts detected. |
| **MeetingService → MeetingNotFoundException** | Dependency | → | Thrown when meeting not found by ID. |
| **ReminderService → MeetingNotFoundException** | Dependency | → | Thrown when meeting not found by ID. |

---

## 4. Package Structure & Separation of Concerns

```
com.rocky.meeting.calendar
│
├── exception/          ─── Custom exceptions for error handling
│   ├── MeetingConflictException.java
│   └── MeetingNotFoundException.java
│
├── model/              ─── Domain models / entities / value objects
│   ├── Meeting.java            (Core entity — aggregate root)
│   ├── MeetingStatus.java      (Enum)
│   ├── Participant.java        (Value Object)
│   ├── RecurrencePattern.java  (Value Object)
│   ├── Reminder.java           (Value Object with mutable sent flag)
│   └── TimeSlot.java           (Value Object — immutable)
│
├── repository/         ─── Data access layer
│   ├── MeetingRepository.java          (Interface — contract)
│   └── ConcurrentMeetingRepository.java (Implementation — thread-safe)
│
└── service/            ─── Business logic layer
    ├── MeetingService.java       (Core CRUD + conflict detection)
    ├── CalendarViewService.java  (View queries by day/week/month)
    └── ReminderService.java      (Reminder management + due detection)
```

Each layer has clear responsibilities:

- **Model layer**: Pure domain objects with no dependency on services or repositories. Encapsulate business rules (e.g., TimeSlot.overlapsWith(), Meeting.isActive()).
- **Repository layer**: Abstract data access behind an interface. The `ConcurrentMeetingRepository` handles thread safety internally using `ReadWriteLock`, keeping the service layer free of synchronization concerns.
- **Service layer**: Orchestrates business operations, validates constraints (conflict detection), and coordinates between multiple models. Each service has a single focus (SRP).

---

## 5. Key Design Decisions

### 5.1 Immutability for Value Objects
`TimeSlot`, `Participant`, and `RecurrencePattern` are immutable (all fields `final`, no setters, defensive copying in constructors). This ensures thread safety and prevents accidental modification after creation.

### 5.2 ReadWriteLock for Concurrency
```
ConcurrentHashMap<String, Meeting>  ← thread-safe map
ReadWriteLock                        ← controls access granularity
├── readLock()  → used by find*(), count()
└── writeLock() → used by save(), deleteById(), deleteAll()
```
- Multiple threads can read simultaneously (no blocking).
- Writes get exclusive access (no reads during write).
- Fair locking (`true` parameter) prevents thread starvation.

### 5.3 Interface-Based Repository
`MeetingRepository` is an interface, allowing:
- Easy swap from in-memory to database-backed implementation
- Mocking for unit tests
- Adding caching or distributed storage without changing service layer

### 5.4 Single Responsibility at Service Level
Instead of one monolithic service:
- **MeetingService**: CRUD + conflict detection + rescheduling
- **CalendarViewService**: Query by calendar granularity (day/week/month)
- **ReminderService**: Reminder addition/removal + due reminder query

This makes the system modular for future additions (e.g., `NotificationService`, `InviteService`, `ExportService`) without modifying existing code.

---

## 6. Data Flow Examples

### 6.1 Create Meeting with Conflict Detection
```
Main → MeetingService.createMeeting()
           │
           ├→ Create TimeSlot(start, end)
           │
           ├→ hasConflict(TimeSlot)
           │      │
           │      └→ repository.findByTimeSlot(TimeSlot)
           │           │
           │           └→ [conflict check]
           │              ├─ empty    → proceed
           │              └─ not empty → throw MeetingConflictException
           │
           ├→ Create Meeting(title, desc, timeSlot)
           ├→ Add participants
           ├→ repository.save(meeting)
           │
           └→ return Meeting
```

### 6.2 Recurring Meeting Generation
```
Main → MeetingService.createRecurringMeeting(start, end, pattern)
           │
           ├→ pattern.generateDates(startTime)
           │      │
           │      └→ [DAILY / WEEKLY / BI_WEEKLY / MONTHLY]
           │           iterate until occurrences or endDate reached
           │
           └→ For each date:
                createMeeting(occurrenceStart, occurrenceEnd, participants)
```

### 6.3 Reminder Due Detection
```
ReminderService.getDueReminders(now)
    │
    ├→ For each active meeting:
    │      │
    │      ├→ For each reminder where !sent:
    │      │      │
    │      │      ├→ triggerTime = meetingStart - timeBeforeMeeting
    │      │      │
    │      │      └→ if triggerTime <= now AND meetingStart > now:
    │      │             → add to due list
    │      │
    │      └→ [next meeting]
    │
    └→ return dueReminders
```

---

## 7. Extensibility Points

| Extension Point | How to Add |
|---|---|
| **New meeting status** | Add value to `MeetingStatus` enum |
| **New recurrence frequency** | Add value to `RecurrencePattern.Frequency` enum + add case in `generateDates()` |
| **New reminder type** | Add value to `Reminder.ReminderType` enum |
| **Database persistence** | Create `JdbcMeetingRepository` or `JpaMeetingRepository` implementing `MeetingRepository` |
| **Email notifications** | Create `NotificationService` that uses `ReminderService.getDueReminders()` |
| **Calendar export** | Create `ExportService` using `CalendarViewService` |
| **Participant availability** | Add method `findConflictingMeetingsForParticipant(String participantId, TimeSlot)` in `MeetingService` |
| **Concurrent request handling** | Already handled by `ConcurrentMeetingRepository` (ReadWriteLock). For distributed systems, use `DistributedMeetingRepository` with Redis/DB locks. |

---

## 8. Thread Safety Analysis

| Scenario | Behavior |
|---|---|
| Multiple reads (findAll, findByDate) | Allowed concurrently via readLock (no blocking) |
| Read while writing | Blocked — writer waits for readers to finish |
| Write while reading | Blocked — readers wait for writer to finish |
| Multiple writes | Queued (fair lock prevents starvation) |
| Concurrent create + find | Atomic — writeLock ensures complete visibility |
| Concurrent reschedule + cancel | Both acquire writeLock → one waits, then sees latest state |