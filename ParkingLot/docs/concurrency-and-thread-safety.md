# Concurrency and Thread Safety in Parking Lot System

## Overview

The Parking Lot system is designed to handle concurrent access from multiple threads (e.g., multiple entry/exit gates). This document describes the thread-safety mechanisms used throughout the codebase.

## Thread-Safe Singleton: `ParkingLot`

```java
private static final ParkingLot INSTANCE = new ParkingLot();
```

The `ParkingLot` class uses the **eager initialization** singleton pattern. The single instance is created at class-loading time, which is inherently thread-safe because class loading is synchronized by the JVM. No explicit synchronization is needed for instance creation.

## ConcurrentHashMap for Active Tickets

```java
private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
```

`ConcurrentHashMap` is used to store active tickets mapped by their ID. This provides:

- **Thread-safe reads and writes** without locking the entire map
- **High concurrency** — multiple threads can read/write different ticket entries simultaneously
- **Atomic operations** like `put()`, `get()`, and `remove()` without external synchronization

## Synchronized Block: `unparkVehicle`

```java
synchronized (ticket) {
    if (ticket.getStatus() == TicketStatus.PAID) {
        throw new RuntimeException("Vehicle already exited");
    }
    double price = pricingStrategy.calculateFees(ticket);
    ticket.getSpot().removeVehicle();
    ticket.close();
    activeTickets.remove(ticketId);
    return price;
}
```

The `unparkVehicle` method uses **synchronized** on the specific `Ticket` object to prevent race conditions:

1. **Duplicate exit prevention**: Two threads cannot process the same ticket simultaneously. The first thread to acquire the lock checks and sets the status to `PAID`, while the second thread will see `PAID` and throw an exception.
2. **Atomic state transition**: The ticket's status, spot release, and fee calculation happen atomically — no other thread can observe an intermediate state.
3. **Per-ticket locking**: Different tickets can be processed concurrently by different threads, maximizing throughput.

## ParkingSpot Thread Safety

### `park()` method

```java
public boolean park(Vehicle vehicle) {
    if (this.vehicle != null) {
        return false;
    }
    this.vehicle = vehicle;
    return true;
}
```

**Note**: The `park()` method is **not** thread-safe in this implementation. If multiple threads attempt to park at the same spot simultaneously, both could see `vehicle == null` and both could assign a vehicle. In a production system, this should be addressed by:

1. **Synchronizing on the spot object**: Use `synchronized (spot) { ... }` when calling `spot.park()`.
2. **Using `AtomicReference`**: Replace `vehicle` field with an `AtomicReference<Vehicle>` and use `compareAndSet(null, vehicle)`.

The `parkVehicle` method in `ParkingLot` does check `spot != null && spot.park(vehicle)`, but the race window still exists between the null check and assignment.

### `removeVehicle()` method

```java
public void removeVehicle() {
    this.vehicle = null;
}
```

This is called from within the `synchronized (ticket)` block in `unparkVehicle`, so it is protected by that synchronization.

## ParkingFloor Thread Safety

```java
public ParkingSpot findAvailableSpot(Vehicle vehicle) {
    for (ParkingSpot spot : spots) {
        if (spot.getVehicle() == null && canFit(vehicle, spot)) {
            return spot;
        }
    }
    return null;
}
```

This method iterates over spots **without synchronization**. In a concurrent environment:

- Multiple threads could receive the same available spot reference.
- The actual parking is gated by `spot.park()`, which acts as a conditional check, but as noted above, it has its own race condition.

## Thread Safety Recommendations

To make the system fully thread-safe, consider these improvements:

### 1. Make `park()` atomic

```java
private final AtomicReference<Vehicle> vehicleRef = new AtomicReference<>(null);

public boolean park(Vehicle vehicle) {
    return vehicleRef.compareAndSet(null, vehicle);
}

public void removeVehicle() {
    vehicleRef.set(null);
}

public Vehicle getVehicle() {
    return vehicleRef.get();
}
```

### 2. Make `findAvailableSpot()` atomic per-spot

Instead of checking and returning separately, integrate the parking logic:

```java
public ParkingSpot findAndPark(Vehicle vehicle) {
    for (ParkingSpot spot : spots) {
        if (spot.canFit(vehicle) && spot.park(vehicle)) {
            return spot;
        }
    }
    return null;
}
```

Where `canFit()` is a read-only check that doesn't require synchronization.

### 3. Use `ReentrantLock` for floor-level coordination

```java
private final ReentrantLock lock = new ReentrantLock();

public ParkingSpot findAvailableSpot(Vehicle vehicle) {
    lock.lock();
    try {
        // iterate and park
    } finally {
        lock.unlock();
    }
}
```

### 4. Consider using `Striped` locks for ticket processing

For finer-grained concurrency on ticket operations, Guava's `Striped` locks can reduce lock contention:

```java
private final Striped<Lock> ticketLocks = Striped.lock(64);

public double unparkVehicle(String ticketId) {
    Lock lock = ticketLocks.get(ticketId);
    lock.lock();
    try {
        // process ticket
    } finally {
        lock.unlock();
    }
}
```

## Summary

| Component | Mechanism | Thread-Safe? |
|-----------|-----------|-------------|
| `ParkingLot` singleton | Eager initialization | ✅ Yes |
| `activeTickets` map | `ConcurrentHashMap` | ✅ Yes |
| `unparkVehicle` | `synchronized (ticket)` | ✅ Yes |
| `parkVehicle` iteration | No synchronization | ⚠️ Partial |
| `ParkingSpot.park()` | Check-then-act without sync | ❌ No |
| `ParkingFloor.findAvailableSpot()` | No synchronization | ❌ No |

The critical path for monetary transactions (`unparkVehicle`) is properly synchronized. The spot allocation path has known race conditions that should be addressed for production readiness.