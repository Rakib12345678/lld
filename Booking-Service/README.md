# BookMyShow Low Level Design (LLD)

A clean, object-oriented, interview-oriented LLD implementation of a simplified BookMyShow system in Java.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Class Diagram](#class-diagram)
3. [Package Structure](#package-structure)
4. [Entity Relationships](#entity-relationships)
5. [Design Patterns Used](#design-patterns-used)
6. [Booking Flow](#booking-flow)
7. [Concurrency Approach](#concurrency-approach)
8. [Running the Project](#running-the-project)
9. [SDE-2 Interview Q&A](#sde-2-interview-qa)

---

## System Overview

```
User
 ├── Search Movie
 ├── Select Theatre
 ├── Select Show
 ├── View Seats
 ├── Select Seats
 ├── Lock Seats
 ├── Create Booking
 ├── Make Payment
 └── Confirm Booking
```

The system prevents two users from booking the same seat for the same show using atomic check-and-lock semantics with a `ReentrantLock`.

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **Single BookingService** (Singleton) | One in-memory store for all entities. Avoids unnecessary abstraction. |
| **Seat vs ShowSeat separation** | Same physical seat can be AVAILABLE for Show 1 and BOOKED for Show 2. |
| **ReentrantLock for concurrency** | Check availability + lock must be atomic to prevent double booking. |
| **Strategy Pattern for pricing** | Open/Closed — new pricing strategies can be added without modifying BookingService. |
| **Lock expiration (5 min)** | Prevents deadlocks from abandoned bookings. |
| **Payment idempotency** | Prevents double-charging on network retries. |

---

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            BOOKMYSHOW CLASS DIAGRAM                         │
└─────────────────────────────────────────────────────────────────────────────┘

                                ┌──────────────┐
                                │  <<enum>>     │
                                │  SeatType     │
                                │──────────────│
                                │  REGULAR      │
                                │  PREMIUM      │
                                │  RECLINER     │
                                └──────────────┘

                                ┌──────────────┐
                                │  <<enum>>     │
                                │  SeatStatus   │
                                │──────────────│
                                │  AVAILABLE     │
                                │  LOCKED        │
                                │  BOOKED        │
                                └──────────────┘

                                ┌──────────────────┐
                                │  <<enum>>         │
                                │  BookingStatus    │
                                │──────────────────│
                                │  CREATED          │
                                │  PAYMENT_PENDING  │
                                │  CONFIRMED        │
                                │  FAILED           │
                                │  CANCELLED        │
                                └──────────────────┘

                                ┌──────────────────┐
                                │  <<enum>>         │
                                │  PaymentStatus    │
                                │──────────────────│
                                │  INITIATED        │
                                │  SUCCESS          │
                                │  FAILED           │
                                └──────────────────┘

       ┌──────────────┐          ┌──────────────┐         ┌──────────────┐
       │    User      │          │    Movie     │         │   Theatre    │
       │──────────────│          │──────────────│         │──────────────│
       │ - id: String │          │ - id: String │         │ - id: String │
       │ - name: Str  │          │ - title: Str │         │ - name: Str  │
       │ - email: Str │          │ - genre: Str │         │──────────────│
       └──────┬───────┘          │ - dur: int   │         │ + addScreen()│
              │                  └──────┬───────┘         │ + getScreens │
              │ booked by              │                  └──────┬───────┘
              │                        │ has                      │ has
              ▼                        ▼                          ▼
       ┌──────────────────────────────────────────────────────────────┐
       │                           Booking                            │
       │──────────────────────────────────────────────────────────────│
       │ - id: String                                                  │
       │ - user: User                                                   │
       │ - show: Show                                                   │
       │ - showSeats: List<ShowSeat>                                    │
       │ - amount: double                                               │
       │ - status: BookingStatus                                        │
       └───────────────────────────┬──────────────────────────────────┘
                                   │ contains
                                   ▼
                    ┌──────────────────────────────┐
                    │          Show                │
                    │──────────────────────────────│
                    │ - id: String                  │
                    │ - movie: Movie                │
                    │ - screen: Screen              │
                    │ - startTime: LocalDateTime    │
                    │ - showSeats: List<ShowSeat>   │
                    └──────┬───────────────────────┘
                           │ contains
                           ▼
           ┌─────────────────────────────────────────┐
           │              ShowSeat                    │
           │─────────────────────────────────────────│
           │ - id: String                             │
           │ - seat: Seat                             │  ← bridges Seat → Show
           │ - price: double                          │
           │ - status: SeatStatus                     │
           │ - lockedBy: String                       │
           │ - lockExpiryTime: LocalDateTime          │
           └──────────────────┬──────────────────────┘
                              │ has
                              ▼
                    ┌──────────────────┐
                    │      Seat        │
                    │──────────────────│
                    │ - id: String     │
                    │ - label: String  │
                    │ - seatType       │
                    └──────────────────┘

                    ┌──────────────────┐     ┌──────────────────┐
                    │     Screen       │     │    Payment       │
                    │──────────────────│     │──────────────────│
                    │ - id: String     │     │ - id: String     │
                    │ - name: String   │     │ - bookingId: Str │
                    │ - seats: List<>  │     │ - amount: double │
                    └──────────────────┘     │ - status         │
                                             └──────────────────┘

                    ┌────────────────────────────────────────────┐
                    │  <<interface>>                             │
                    │  PricingStrategy                           │
                    │────────────────────────────────────────────│
                    │  + calculatePrice(Seat): double            │
                    └────────────────────────────────────────────┘
                              ▲                   ▲
                              │                   │
                    ┌─────────────────┐  ┌──────────────────────────┐
                    │DefaultPricing   │  │ WeekendPricingStrategy   │
                    │    Strategy     │  │ (decorator — +20%)       │
                    │ REGULAR: ₹200  │  │ wraps another strategy   │
                    │ PREMIUM: ₹300  │  └──────────────────────────┘
                    │ RECLINER: ₹500 │
                    └─────────────────┘

                    ┌────────────────────────────────────────────┐
                    │        BookingService (SINGLETON)          │
                    │────────────────────────────────────────────│
                    │ - INSTANCE: BookingService                  │
                    │ - users: Map<String, User>                  │
                    │ - movies: Map<String, Movie>                │
                    │ - theatres: Map<String, Theatre>            │
                    │ - shows: Map<String, Show>                  │
                    │ - bookings: Map<String, Booking>            │
                    │ - payments: Map<String, Payment>            │
                    │ - bookingLock: ReentrantLock                │
                    │────────────────────────────────────────────│
                    │ + getInstance()                             │
                    │ + addUser() / getUser()                     │
                    │ + addMovie() / getMovie() / searchMovies()  │
                    │ + addTheatre() / getTheatre()               │
                    │ + addShow() / getShow()                     │
                    │ + getShowsForMovie() / getShowsForTheatre() │
                    │ + createShowSeats(show, pricingStrategy)    │
                    │ + showAvailableSeats(show)                  │
                    │ + createBooking(user, show, seatIds) ★      │
                    │ + makePayment(bookingId)                    │
                    │ + cancelBooking(bookingId)                  │
                    │ + getBooking() / getPayment()               │
                    └────────────────────────────────────────────┘

                    ★ = Atomic operation (protected by ReentrantLock)
```

### Dependency/Association Summary

| Class | Relationship | Target |
|-------|-------------|--------|
| `Theatre` | has many | `Screen` |
| `Screen` | has many | `Seat` |
| `Show` | has one | `Movie` |
| `Show` | has one | `Screen` |
| `Show` | has many | `ShowSeat` |
| `ShowSeat` | has one | `Seat` |
| `Booking` | has one | `User` |
| `Booking` | has one | `Show` |
| `Booking` | has many | `ShowSeat` |
| `Payment` | references | `Booking` (by ID) |

---

## Package Structure

```
com.bookmyshow/
├── Main.java                          # Interview demo
├── model/
│   ├── User.java                      # User entity
│   ├── Movie.java                     # Movie entity
│   ├── Theatre.java                   # Theatre with List<Screen>
│   ├── Screen.java                    # Screen with List<Seat>
│   ├── Seat.java                      # Physical seat (no booking state)
│   ├── Show.java                      # Show = Movie + Screen + startTime + List<ShowSeat>
│   ├── ShowSeat.java                  # Seat state per show (AVAILABLE/LOCKED/BOOKED)
│   ├── Booking.java                   # Booking = User + Show + List<ShowSeat>
│   └── Payment.java                   # Payment with idempotency support
├── enums/
│   ├── SeatType.java                  # REGULAR, PREMIUM, RECLINER
│   ├── SeatStatus.java                # AVAILABLE, LOCKED, BOOKED
│   ├── BookingStatus.java             # CREATED, PAYMENT_PENDING, CONFIRMED, FAILED, CANCELLED
│   └── PaymentStatus.java             # INITIATED, SUCCESS, FAILED
├── strategy/
│   ├── PricingStrategy.java           # Strategy Pattern interface
│   ├── DefaultPricingStrategy.java    # Flat rates: ₹200/300/500
│   └── WeekendPricingStrategy.java    # 20% surcharge decorator
└── service/
    └── BookingService.java            # Singleton — handles everything
```

---

## Entity Relationships

```
Theatre ──has many──> Screen ──has many──> Seat

Show ──has one──> Movie
Show ──has one──> Screen
Show ──has many──> ShowSeat ──has one──> Seat

Booking ──has one──> User
Booking ──has one──> Show
Booking ──has many──> ShowSeat

Payment ──references──> Booking (by bookingId)
```

### Why `Seat` and `ShowSeat` are separate

`Seat` represents a **physical seat** in a Screen (e.g., A1 in Screen 1). It does NOT store booking status because the same physical seat can be:
- **Show 1** → BOOKED
- **Show 2** → AVAILABLE
- **Show 3** → AVAILABLE

`ShowSeat` represents the **state of that Seat for a specific Show**, including:
- `price` (determined by PricingStrategy)
- `status` (AVAILABLE / LOCKED / BOOKED)
- `lockedBy` (which user locked it)
- `lockExpiryTime` (when the lock expires)

---

## Design Patterns Used

### 1. Singleton Pattern — `BookingService`

```
private static final BookingService INSTANCE = new BookingService();
private BookingService() {}
public static BookingService getInstance() { return INSTANCE; }
```

**Why:** We need a single in-memory store for the entire system. Prevents multiple instances with inconsistent data. In a real system this would become a database layer.

### 2. Strategy Pattern — `PricingStrategy`

```java
@FunctionalInterface
public interface PricingStrategy {
    double calculatePrice(Seat seat);
}
```

**Implementations:**
- `DefaultPricingStrategy` — flat rates: REGULAR ₹200, PREMIUM ₹300, RECLINER ₹500
- `WeekendPricingStrategy` — wraps another strategy and adds 20% surcharge

**Why:** Pricing logic is externalized from BookingService. Open/Closed Principle — new strategies (holiday, early-bird, dynamic) can be added without modifying existing code.

### 3. Decorator Pattern — `WeekendPricingStrategy`

```java
public class WeekendPricingStrategy implements PricingStrategy {
    private final PricingStrategy baseStrategy;
    public double calculatePrice(Seat seat) {
        return baseStrategy.calculatePrice(seat) * 1.20;
    }
}
```

**Why:** Demonstrates composability — strategies can wrap other strategies to layer pricing rules.

---

## Booking Flow

```
Step 1: createBooking(user, show, seatIds)
  ├── (LOCK acquired)
  ├── Resolve ShowSeats from seatIds
  ├── Release any expired locks
  ├── Check ALL seats are AVAILABLE
  ├── If any unavailable → throw exception (atomic failure, no partial locking)
  ├── Lock ALL seats → LOCKED, lockedBy=userId, lockExpiryTime=now+5min
  ├── Calculate total price
  ├── Create Booking → PAYMENT_PENDING
  ├── Store booking
  └── (LOCK released)
         │
         ▼
Step 2: makePayment(bookingId)
  ├── Idempotency check: if payment already successful → return existing
  ├── Validate booking is PAYMENT_PENDING
  ├── Create Payment → INITIATED
  ├── Mock payment gateway call
  ├── On SUCCESS:
  │     ├── Payment → SUCCESS
  │     ├── Booking → CONFIRMED
  │     └── ShowSeats LOCKED → BOOKED
  └── On FAILED:
        ├── Payment → FAILED
        ├── Booking → FAILED
        └── ShowSeats LOCKED → AVAILABLE

Step 3 (optional): cancelBooking(bookingId)
  ├── Validate booking is CONFIRMED
  ├── Release all ShowSeats → BOOKED → AVAILABLE
  └── Booking → CANCELLED
```

### Atomicity Guarantee

```
CHECK Availability  ──┐
                      ├──> Atomic (protected by ReentrantLock)
LOCK Seats          ──┘
```

Without atomicity:
- Thread 1: checks A1 → AVAILABLE
- Thread 2: checks A1 → AVAILABLE ← **both see available**
- Thread 1: locks A1
- Thread 2: locks A1 ← **DOUBLE BOOKING!**

With ReentrantLock, Thread 2 cannot start checking until Thread 1 completes the entire check+lock sequence.

---

## Concurrency Approach

```java
private final ReentrantLock bookingLock = new ReentrantLock();

public Booking createBooking(User user, Show show, List<String> seatIds) {
    bookingLock.lock();
    try {
        // check availability
        // lock seats
        // create booking
    } finally {
        bookingLock.unlock();
    }
}
```

**Why ReentrantLock over `synchronized`:**
- More flexible (tryLock, timed lock acquisition)
- Clearer separation of critical section
- Interview-friendly — demonstrates understanding of java.util.concurrent

---

## Concurrency Optimizations

### Problem: Global Lock Contention

The initial implementation used a **single global `ReentrantLock`** for ALL booking operations. This meant:
- User A booking A1 in **Show 1** blocks User B booking B1 in **Show 2** (unnecessary contention)
- User A booking A1 blocks User B booking B1 in the **same show** (correct, but could be finer)

### Optimization 1: Per-Show Lock (Striped Lock)

```java
private final ConcurrentHashMap<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();

private ReentrantLock getShowLock(String showId) {
    return showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
}
```

| Scenario | Before (Global Lock) | After (Per-Show Lock) |
|----------|---------------------|----------------------|
| User A books Show 1, User B books Show 2 | **Blocked** (unnecessary) | **Parallel** (no contention) |
| User A books Show 1, User B books Show 1 | **Blocked** (correct) | **Blocked** (correct) |
| User A books Show 1.A1, User B books Show 1.B1 | **Blocked** | **Blocked** (can optimize further) |

### Optimization 2: Per-Seat Lock with Sorted Acquisition

For the same show, different seats should NOT contend:

```java
private final ConcurrentHashMap<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();

// Sort seat IDs before acquiring locks to prevent deadlock
private void acquireSeatLocks(String showId, List<String> sortedSeatIds) {
    for (String seatId : sortedSeatIds) {
        String lockKey = showId + ":" + seatId;
        ReentrantLock seatLock = seatLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        seatLock.lock();
    }
}
```

### Deadlock Prevention: Lock Ordering

**Without sorting (DEADLOCK scenario):**
```
User X: lock(A1) → lock(A2)
User Y: lock(A2) → lock(A1)
Result: X holds A1 waiting for A2, Y holds A2 waiting for A1 → DEADLOCK!
```

**With sorting (DEADLOCK PREVENTED):**
```
User X: sort([A1, A2]) → [A1, A2] → lock(A1) → lock(A2)
User Y: sort([A2, A1]) → [A1, A2] → lock(A1) → waits (X holds A1) → X finishes → Y gets A1 → lock(A2)
Result: Both request locks in the same order → one waits, no deadlock.
```

This is the standard **lock ordering** technique (Dijkstra's algorithm for deadlock prevention).

### Two-Level Lock Hierarchy in `createBooking()`

```java
public Booking createBooking(User user, Show show, List<String> seatIds) {
    // Level 1: Per-show lock
    ReentrantLock showLock = getShowLock(show.getId());
    showLock.lock();
    try {
        // Check ALL seats BEFORE acquiring seat-level locks
        // (atomic failure guarantee — if any seat is unavailable, no locks are taken)
        
        List<String> sortedSeatIds = new ArrayList<>(seatIds);
        Collections.sort(sortedSeatIds);
        // Level 2: Per-seat locks in sorted order
        acquireSeatLocks(show.getId(), sortedSeatIds);
        try {
            // Re-check availability (safety net)
            // Lock all seats and create booking
        } finally {
            releaseSeatLocks(show.getId(), sortedSeatIds);  // reverse order
        }
    } finally {
        showLock.unlock();
    }
}
```

| Scenario | Contention? | Reason |
|----------|------------|--------|
| Different shows | **No** | Per-show locks are independent |
| Same show, same seats | **Yes** (serialized) | Competing for same seats |
| Same show, different seats (e.g., A1,A2 vs B1,B2) | **No** | Per-seat locks are independent |
| Same show, overlapping seats (e.g., A1,A2 vs A2,A3) | **Partial** (A2 only) | Only the overlapping seat serializes |

---

## Distributed System Approaches

For a production distributed system (multiple service instances), the in-memory `ReentrantLock` must be replaced with a distributed lock mechanism.

### Approach A: Redis Distributed Lock (Redlock)

```
Key pattern: "show:{showId}:seat:{seatLabel}:lock"
Value: {userId, bookingId, expiryTimestamp}
TTL: 300 seconds (5 minutes)
```

**Atomic check-and-lock using Lua script:**

```lua
-- KEYS    = seat lock keys (e.g., ["show:sh1:seat:A1", "show:sh1:seat:A2"])
-- ARGV[1] = current timestamp
-- ARGV[2] = userId
-- ARGV[3] = lock duration in seconds

-- Check all seats
for i, key in ipairs(KEYS) do
    local value = redis.call('GET', key)
    if value then
        -- Parse existing lock
        local expiry = tonumber(string.match(value, 'expiry:(%d+)'))
        if expiry and expiry > tonumber(ARGV[1]) then
            return 'SEAT_LOCKED:' .. key  -- Seat is still locked
        end
        -- Lock expired — will be overwritten
    end
end

-- All seats available or expired — lock all
for i, key in ipairs(KEYS) do
    local lockValue = ARGV[2] .. '|expiry:' .. (tonumber(ARGV[1]) + tonumber(ARGV[3]) * 1000)
    redis.call('SET', key, lockValue, 'NX', 'EX', ARGV[3])
end
return 'SUCCESS'
```

**Pros:** Fast (in-memory), TTL handles auto-expiry, Lua guarantees atomicity  
**Cons:** Clock drift concerns, Redis is not a source of truth (data can be lost)

### Approach B: Database Row-Level Locking (PostgreSQL)

```sql
-- Table: show_seats
-- Columns: id, show_id, seat_label, status, version, locked_by, lock_expiry

BEGIN TRANSACTION;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- Lock rows with SELECT FOR UPDATE (sorted to prevent deadlock)
SELECT * FROM show_seats
WHERE show_id = 'sh1' AND seat_label IN ('A1', 'A2')
ORDER BY seat_label
FOR UPDATE;

-- Check all seats are AVAILABLE in application code
-- If all available:

UPDATE show_seats
SET status = 'LOCKED',
    locked_by = 'user1',
    lock_expiry = NOW() + INTERVAL '5 minutes',
    version = version + 1
WHERE show_id = 'sh1'
  AND seat_label IN ('A1', 'A2')
  AND status = 'AVAILABLE'
  AND version = <expected_version>;

-- If rows_affected < 2 → ROLLBACK (concurrent modification detected)

COMMIT;
```

**Optimistic locking alternative (no `FOR UPDATE`):**

```sql
-- Read current version
SELECT version, status FROM show_seats WHERE show_id = 'sh1' AND seat_label = 'A1';
-- version = 5, status = 'AVAILABLE'

-- Atomically update with version check
UPDATE show_seats
SET status = 'LOCKED', version = version + 1
WHERE show_id = 'sh1' AND seat_label = 'A1' AND version = 5;
-- If affected rows = 0 → someone else modified it, retry
```

**Pros:** ACID guarantees, data integrity, familiar to teams  
**Cons:** Slower, connection pool contention, row locks escalate

### Approach C: Redis + Database Hybrid (Recommended for Production)

```
1. Redis lock (fast path):
   SET show:sh1:seat:A1 "user1|booking:b1" NX EX 5
   → Success: proceed
   → Failure: seat locked by another user → return error

2. If Redis lock acquired, update DB (source of truth):
   UPDATE show_seats SET status = 'LOCKED', version = version + 1
   WHERE show_id = 'sh1' AND seat_label = 'A1' AND version = <expected>

3. If DB update fails → release Redis lock → return failure

4. TTL handles cleanup if service crashes mid-operation
```

**Key insight:** Redis provides speed and lock management; the database provides durability and the source of truth. The two-phase approach gives the best of both worlds.

### Distributed Deadlock Prevention

In all distributed approaches, the same **lock ordering principle** applies:

```
1. Sort seat labels: [seat1, seat2, seat3]
2. Acquire locks in sorted order (Redis SET NX or DB SELECT FOR UPDATE)
3. If any lock fails, release all acquired locks (in reverse order)
4. If all locks acquired, proceed with booking
```

This is identical to the single-process approach but uses distributed primitives.

---

## Running the Project

```bash
# Compile
mvn compile

# Run demo
mvn exec:java -Dexec.mainClass="com.bookmyshow.Main"

# Or compile and run manually
mvn compile
java -cp target/classes com.bookmyshow.Main
```

### Expected Output

```
========================================
   BOOKMYSHOW LLD DEMO
========================================

===== AVAILABLE SEATS =====
A1 -> AVAILABLE -> ₹200
A2 -> AVAILABLE -> ₹200
A3 -> AVAILABLE -> ₹300
A4 -> AVAILABLE -> ₹500

===== USER 1 (Alice) BOOKING =====
Seats locked: A1, A2
Payment successful
Booking confirmed!

===== USER 2 (Bob) BOOKING =====
Seat A1 is not available
Booking failed
Double booking prevented!

===== CANCELLATION =====
Booking cancelled
A1 -> AVAILABLE
A2 -> AVAILABLE

===== USER 2 (Bob) BOOKING AGAIN =====
Seats locked: A1, A3
Booking successful
```

---

## SDE-2 Interview Q&A

### Q1: Why did you separate Seat and ShowSeat?

**A:** `Seat` represents a **physical seat** in a Screen (e.g., chair A1 in Screen 1). The same physical seat can have different states for different shows:
- **Show 1 (3 PM)**: A1 is BOOKED
- **Show 2 (6 PM)**: A1 is AVAILABLE

If booking status were stored in `Seat` itself, we'd need to reset it between shows or create duplicate Seat objects per show. `ShowSeat` bridges a Seat to a specific Show and tracks its price, status, lock holder, and lock expiry. This is a cleaner separation of concerns and maps well to how real systems work (a seat's availability is contextual to the show).

### Q2: Why did you make BookingService a Singleton?

**A:** For an in-memory LLD, we need a **single source of truth** for all entities. Multiple instances would lead to inconsistent data (User created in Service A but not visible to Service B). Singleton ensures all operations go through the same in-memory stores.

**Trade-off:** In a real distributed system, we'd replace the in-memory Maps with a database and the Singleton with a Spring `@Service` (which is singleton-scoped anyway). The Singleton here is simply an honest representation of "these maps should live in one place."

### Q3: How do you handle concurrent booking attempts for the same seat?

**A:** I use a `ReentrantLock` to make the **check+lock sequence atomic**:

```
LOCK acquired
  → Check ALL requested seats are AVAILABLE
  → If any unavailable → throw exception (fail fast, no partial locking)
  → Lock ALL seats (set status = LOCKED, lockedBy, lockExpiryTime)
LOCK released
```

This prevents the race condition where two threads both check availability simultaneously, both see `AVAILABLE`, and both proceed to lock the same seat. Without atomicity, we'd get double booking.

**Why ReentrantLock vs synchronized?** ReentrantLock provides:
- `tryLock()` for non-blocking attempts
- Fairness policy to prevent starvation
- More explicit control over the critical section

### Q4: Explain the pricing strategy pattern and why it's useful.

**A:** The `PricingStrategy` interface uses **Strategy Pattern** to externalize pricing logic:

```java
@FunctionalInterface
public interface PricingStrategy {
    double calculatePrice(Seat seat);
}
```

**Implementations:**
- `DefaultPricingStrategy`: flat rates (REGULAR ₹200, PREMIUM ₹300, RECLINER ₹500)
- `WeekendPricingStrategy`: wraps base strategy + 20% surcharge (also demonstrates Decorator Pattern)

**Why this matters in interviews:**
- **Open/Closed Principle**: BookingService is closed for modification but open for extension. We can add `HolidayPricingStrategy` or `DynamicPricingStrategy` without touching BookingService.
- **Testability**: Strategies can be unit-tested independently.
- **Composability**: Strategies can wrap other strategies (WeekendPricingStrategy wraps any base strategy).

### Q5: How does lock expiration work and why is it necessary?

**A:** When a user locks seats during booking, the lock has a **5-minute expiry**:

```java
ss.setLockExpiryTime(LocalDateTime.now().plusMinutes(5));
```

The `releaseExpiredLock()` method is called before any availability check:

```java
private void releaseExpiredLock(ShowSeat showSeat) {
    if (showSeat.getStatus() == SeatStatus.LOCKED
            && showSeat.getLockExpiryTime() != null
            && LocalDateTime.now().isAfter(showSeat.getLockExpiryTime())) {
        showSeat.setStatus(SeatStatus.AVAILABLE);
        showSeat.setLockedBy(null);
        showSeat.setLockExpiryTime(null);
    }
}
```

**Why necessary:** Without expiration, if a user locks seats and then abandons the booking (closes browser, crashes), those seats would be locked **forever**. Expiration ensures seats are eventually released. This is the same approach used by real systems (Zomato locks cart items for 10 minutes, BookMyShow locks seats for 5 minutes).

### Q6: How did you implement atomic booking failure?

**A:** If a user requests seats A1, A2, A3 and **A3 is unavailable**, the entire booking fails — A1 and A2 must NOT remain locked. This is guaranteed by:

```java
// Step 1: Check ALL seats first
for (ShowSeat ss : requestedSeats) {
    if (ss.getStatus() != SeatStatus.AVAILABLE) {
        throw new IllegalStateException("...");  // Fail before locking anything
    }
}

// Step 2: Only now lock everything
for (ShowSeat ss : requestedSeats) {
    ss.setStatus(SeatStatus.LOCKED);
    // ...
}
```

Because this entire method is under `ReentrantLock`, the check and lock are atomic. No other thread can interleave between checking and locking.

### Q7: Explain the payment flow and idempotency.

**A:** **Payment flow:**
```
Booking (PAYMENT_PENDING)
  → Create Payment (INITIATED)
  → Mock gateway call
  → SUCCESS: Payment → SUCCESS, Booking → CONFIRMED, Seats → BOOKED
  → FAILED:  Payment → FAILED, Booking → FAILED, Seats → AVAILABLE
```

**Idempotency:** Before processing a payment, we check if a **successful payment already exists** for this booking:

```java
for (Payment existing : payments.values()) {
    if (existing.getBookingId().equals(bookingId)
            && existing.getStatus() == PaymentStatus.SUCCESS) {
        return existing;  // don't charge again
    }
}
```

**Why idempotency matters:** In real systems, network retries can cause duplicate payment requests. Without idempotency, a user could be charged twice for the same booking. A production system would use idempotency keys (client-generated unique request identifiers) to handle this robustly.

### Q8: What would you change for a production system?

**A:** For a production system, I'd change:

1. **Database instead of in-memory Maps**: Replace `HashMap` with a relational DB (PostgreSQL) with proper transaction isolation (`SERIALIZABLE` or `SELECT FOR UPDATE`).
2. **Distributed lock instead of ReentrantLock**: In a multi-instance deployment, use Redis/Redlock or ZooKeeper for distributed locking.
3. **Async payment processing**: Use message queues (Kafka/RabbitMQ) for payment flow to decouple booking from payment.
4. **Real payment gateway**: Integrate with Stripe/Razorpay with webhooks for payment confirmation.
5. **Optimistic concurrency**: Use version columns in DB to detect conflicts without pessimistic locks.
6. **Caching**: Redis cache for seat availability to reduce DB load.
7. **Monitoring**: Metrics for booking success rate, payment latency, lock contention.
8. **Cancellation policies**: Time-based cancellation windows, cancellation fees, refund processing.
9. **Notification service**: Email/SMS confirmations for booking and cancellation.

### Q9: How does this design handle the case where a user tries to book seats that have expired locks?

**A:** This is handled transparently. When a LOCKED seat's `lockExpiryTime` has passed, it's automatically released:

1. `createBooking()` is called
2. Inside the critical section, `releaseExpiredLock()` runs for each requested seat
3. If a seat was LOCKED but expired, it becomes AVAILABLE
4. The availability check then succeeds for that seat
5. The new user gets the seat

This means the original holder's lock silently expires, and the new user can book without any explicit "release" step.

### Q10: What test cases would you write for this system?

**A:** Key test cases:

1. **Happy path**: User books available seats → payment → confirmed
2. **Double booking prevention**: Two concurrent requests for same seat → one succeeds, one fails
3. **Atomic failure**: Request for A1, A2 where A2 is unavailable → neither seat is locked
4. **Lock expiration**: Lock a seat → wait past expiry → verify seat is available again
5. **Cancellation**: Book seats → cancel → verify seats are AVAILABLE
6. **Payment idempotency**: Call makePayment() twice with same booking → only one payment created
7. **Edge cases**: Empty seat list, invalid user/movie/show IDs, duplicate booking request
8. **Pricing strategies**: Verify DefaultPricingStrategy and WeekendPricingStrategy calculate correct amounts