Here's a formal UML class diagram you can reference during the interview. I'll create a text-based UML that you can either memorize or draw on a whiteboard.

---

## UML Class Diagram - BookMyShow LLD

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLASS DIAGRAM                               │
│                    BookMyShow Booking System                        │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│   Theatre    │     │    Screen    │     │      Seat        │
├──────────────┤     ├──────────────┤     ├──────────────────┤
│ - id: String │◄────│ - id: String │◄────│ - id: String     │
│ - name: Str  │1   *│ - name: Str  │1   *│ - label: String  │
│ - city: Str  │     │ - seats:List │     │ - type: SeatType │
├──────────────┤     ├──────────────┤     ├──────────────────┤
│ + addScreen()│     │ + addSeat()  │     │ + getters()      │
│ + getters()  │     │ + getSeats() │     └──────────────────┘
└──────────────┘     └──────────────┘

┌──────────────┐     ┌──────────────┐
│    Movie     │     │     Show     │
├──────────────┤     ├──────────────┤
│ - id: String │     │ - id: String │
│ - title: Str │     │ - movie:Movie│◄──── 1
│ - genre: Str │     │ - screen:Scr │◄──── 1
│ - duration:  │     │ - startTime  │
│   int        │     │ - showSeats  │
├──────────────┤     ├──────────────┤
│ + getters()  │     │ + addShowSeat│
└──────────────┘     │ + getters()  │
                     └──────┬───────┘
                            │1
                            │
                            │*  ┌──────────────────┐
                            └──>│   ShowSeat       │
                                ├──────────────────┤
┌──────────────┐               │ - id: String     │
│    User      │               │ - seat: Seat ◄───┼──── 1
├──────────────┤               │ - price: double  │
│ - id: String │               │ - status:SeatSts │
│ - name: Str  │               └────────┬─────────┘
│ - email: Str │                        │
├──────────────┤                        │* (ordered)
│ + getters()  │                        │
└──────┬───────┘                        │
       │1                               │
       │                                │
       │   ┌────────────────────────────┘
       │   │
       │   │  ┌──────────────────┐
       │   │  │    Booking       │
       │   │  ├──────────────────┤
       └───┼──┤ - id: String     │
           │  │ - user:User ◄────┘
           │  │ - show:Show ◄────┘
           │  │ - showSeats:List  │
           │  │ - amount: double  │
           │  │ - status:BkngSts  │
           │  │ - payment:Pay ◄──┼──── 0..1
           │  ├──────────────────┤
           │  │ + getPayment()   │
           │  │ + setPayment()   │
           │  │ + getAmount()    │
           │  └──────────────────┘
           │
           │  ┌──────────────────┐
           │  │    Payment       │
           │  ├──────────────────┤
           │  │ - id: String     │
           │  │ - amount: double │
           │  │ - status:PaySts  │
           │  ├──────────────────┤
           │  │ + getters()      │
           │  └──────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────────┐
│                       ENUMS                                     │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│  │  SeatType    │  │  SeatStatus  │  │  BookingStatus   │      │
│  ├──────────────┤  ├──────────────┤  ├──────────────────┤      │
│  │ REGULAR      │  │ AVAILABLE    │  │ CREATED          │      │
│  │ PREMIUM      │  │ LOCKED       │  │ PAYMENT_PENDING  │      │
│  │ RECLINER     │  │ BOOKED       │  │ CONFIRMED        │      │
│  └──────────────┘  └──────────────┘  │ FAILED           │      │
│                                       │ CANCELLED        │      │
│  ┌──────────────────────┐             └──────────────────┘      │
│  │  PaymentStatus       │                                       │
│  ├──────────────────────┤                                       │
│  │ INITIATED, SUCCESS,  │                                       │
│  │ FAILED               │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘
```

## Service Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              BookingService (Facade)                     │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │  Users:    addUser()  getUser()                    │  │  │
│  │  │  Movies:   addMovie() getMovie() searchMovies()    │  │  │
│  │  │  Theatres: addTheatre() getTheatre() searchByCity()│  │  │
│  │  │  Shows:    addShow() getShow() searchShows()       │  │  │
│  │  │  Seats:    showAvailableSeats() createShowSeats()  │  │  │
│  │  │  Booking:  createBooking() cancelBooking()         │  │  │
│  │  │  Payment:  makePayment()                           │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │ delegates to                       │
│           ┌─────────────────┼──────────────────────┐            │
│           ▼                 ▼                      ▼             │
│  ┌──────────────┐  ┌────────────────┐  ┌──────────────────────┐ │
│  │ UserService  │  │ MovieService   │  │ TheatreService       │ │
│  ├──────────────┤  ├────────────────┤  ├──────────────────────┤ │
│  │ + addUser()  │  │ + addMovie()   │  │ + addTheatre()       │ │
│  │ + getUser()  │  │ + getMovie()   │  │ + getTheatre()       │ │
│  └──────────────┘  │ + searchMovies │  │ + searchByCity()     │ │
│                     └────────────────┘  └──────────────────────┘ │
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────────┐ │
│  │ ShowService    │  │ SeatLockMgr    │  │ BookingManager     │ │
│  ├────────────────┤  ├────────────────┤  ├────────────────────┤ │
│  │ + addShow()    │  │ + getShowLock()│  │ + createBooking()  │ │
│  │ + getShow()    │  │ + lockSeats()  │  │ + makePayment() ◄──┼──┐
│  │ + searchShows()│  │ + releaseSeats │  │ + cancelBooking()  │  │
│  └────────────────┘  └────────────────┘  └────────────────────┘  │
│                                                                  │
│  ┌──────────────────────┐          ┌─────────────────────────┐   │
│  │ PaymentService       │          │ <<interface>>           │   │
│  ├──────────────────────┤          │ PricingStrategy         │   │
│  │ + processPayment(    │          ├─────────────────────────┤   │
│  │     Booking):Payment │          │ + calculatePrice(Seat)  │   │
│  └──────────────────────┘          └──────────┬──────────────┘   │
│                   ▲                           │ implements       │
│                   │              ┌────────────┼────────────┐     │
│                   └──────────────│            │            │     │
│                                  ▼            ▼            ▼     │
│  ┌────────────────────────┐ ┌────────────────┐ ┌──────────────┐  │
│  │DefaultPricingStrategy  │ │WeekendPricing  │ │ (Future)     │  │
│  │                        │ │Strategy        │ │ PromoPricing │  │
│  │ REGULAR = ₹200         │ │ REGULAR = ₹250 │ │ Strategy     │  │
│  │ PREMIUM = ₹300         │ │ PREMIUM = ₹375 │ │              │  │
│  │ RECLINER = ₹500        │ │ RECLINER = ₹625│ └──────────────┘  │
│  └────────────────────────┘ └────────────────┘                    │
└─────────────────────────────────────────────────────────────────┘
```

## Relationship Summary Table

| From | To | Type | Multiplicity | Notes |
|---|---|---|---|---|
| **Theatre** | **Screen** | Composition | 1 → * | Screen cannot exist without Theatre |
| **Screen** | **Seat** | Composition | 1 → * | Seat cannot exist without Screen |
| **Show** | **Screen** | Association | 1 → 1 | Show is screened on one Screen |
| **Show** | **Movie** | Association | 1 → 1 | Show screens one Movie |
| **Show** | **ShowSeat** | Composition | 1 → * | ShowSeat lifecycle tied to Show |
| **ShowSeat** | **Seat** | Association | * → 1 | Links to physical Seat configuration |
| **Booking** | **User** | Association | * → 1 | User can have many Bookings |
| **Booking** | **Show** | Association | * → 1 | Many Bookings can exist for one Show |
| **Booking** | **ShowSeat** | Association | 1 → * | Booking contains ordered list of ShowSeats |
| **Booking** | **Payment** | Composition | 1 → 0..1 | Payment owned by Booking; null until paid |

## Design Patterns Used

| Pattern | Where | Why |
|---|---|---|
| **Facade** | `BookingService` | Single public API; hides complexity |
| **Strategy** | `PricingStrategy` | Pluggable pricing algorithms |
| **Composition** | Theatre→Screen, Booking→Payment | Strong ownership lifecycle |

---

You can draw this on a whiteboard in ~5 minutes during the interview. Start with the models, then add relationships, then the service layer on the side.