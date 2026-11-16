# Parking Lot System

A concurrent parking lot management system built in Java.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   ParkingLot (Singleton)                │
├─────────────────────────────────────────────────────────┤
│  activeTickets: ConcurrentHashMap<String, Ticket>       │
│  floors: List<ParkingFloor>                             │
│  pricingStrategy: FeeStrategy                           │
└─────────────────────────────────────────────────────────┘
         │
         ├── ParkingFloor
         │      ├── floorNumber
         │      └── spots: List<ParkingSpot>
         │              ├── VehicleSize (SMALL, MEDIUM, LARGE)
         │              └── Vehicle
         │
         ├── Ticket
         │      ├── vehicle, spot
         │      ├── entryTime, exitTime
         │      ├── price, status (ACTIVE/PAID)
         │      └── synchronized for unpark
         │
         └── FeeStrategy (interface)
                ├── DefaultFeesStrategy (flat rate)
                └── HourlyPricingStrategy (per-hour)
```

## Core Entities

### Vehicle & Subtypes
- **Vehicle** — abstract class with `vehicleSize` and `no` fields
- **Bike** — `VehicleSize.SMALL`
- **Car** → `VehicleSize.MEDIUM`
- **Truck** → `VehicleSize.LARGE`

### Parking Spot
- A spot has a **size** (`VehicleSize`) and can hold one **Vehicle** at a time
- A vehicle can park in a spot if the spot's size ordinal >= vehicle's size ordinal (e.g., a `SMALL` vehicle can park in `MEDIUM` or `LARGE` spots)
- Methods: `park(vehicle)`, `removeVehicle()`, `getVehicle()`

### Parking Floor
- Contains a list of `ParkingSpot`s
- `findAvailableSpot(vehicle)` — iterates spots and returns the first available one that fits the vehicle

### Ticket
- Created when a vehicle parks: stores vehicle, spot, entry time
- Status: `ACTIVE` → `PAID` after unparking
- `getEntry()` returns entry time in milliseconds for fee calculation

## Fee Calculation

| Strategy | SMALL | MEDIUM | LARGE |
|----------|-------|--------|-------|
| **Default** (flat) | ₹10 | ₹20 | ₹30 |
| **Hourly** | ₹20/hr | ₹50/hr | ₹100/hr |

## Thread Safety

| Component | Mechanism | Status |
|-----------|-----------|--------|
| `ParkingLot` singleton | Eager initialization (JVM-safe) | ✅ |
| `activeTickets` map | `ConcurrentHashMap` | ✅ |
| `unparkVehicle` | `synchronized(ticket)` block | ✅ |
| `parkVehicle` | `spot.park()` (check-then-act) | ⚠️ needs `AtomicReference` |
| `ParkingFloor.findAvailableSpot()` | No synchronization | ⚠️ needs floor-level lock |

## Build & Run

```bash
# Compile
mvn compile

# Run
java -cp target/classes model.Main
```

## Sample Output

```
========= Parking Lot Initialized =========
Floors: 2 | Total Spots: 7
Small: A1, A2, A3 | Medium: B1, B2 | Large: C1, C2

========= Park Vehicles =========
Bike parked → Ticket: 57b0e9a1-... | Spot: A1 | Entry: 2026-08-29T18:46:26.698239
Car parked   → Ticket: 148d1ae4-... | Spot: B1 | Entry: 2026-08-29T18:46:26.705352
Truck parked → Ticket: 5f748d00-... | Spot: C1 | Entry: 2026-08-29T18:46:26.705643
Bike2 parked → Ticket: 8c9fa5d4-... | Spot: A2 | Entry: 2026-08-29T18:46:26.705953

========= Unpark Vehicles =========
Bike unparked → Fee: ₹20.0 | Spot A1 is now free
Car unparked  → Fee: ₹50.0 | Spot B1 is now free
Truck unparked → Fee: ₹100.0 | Spot C1 is now free

========= Duplicate Exit Attempt =========
Expected error: Invalid ticket
```

## Documentation

- [Concurrency & Thread Safety](docs/concurrency-and-thread-safety.md) — detailed analysis of thread-safety mechanisms and recommendations.