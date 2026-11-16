package com.bookmyshow.service;

import com.bookmyshow.model.*;
import com.bookmyshow.strategy.PricingStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BookingService is the SINGLE public-facing service that orchestrates all
 * booking operations. It composes dedicated domain services to maintain
 * clean separation of concerns:
 *
 * - UserService       → user management
 * - MovieService      → movie management
 * - TheatreService    → theatre management
 * - ShowService       → show management & search
 * - SeatLockManager   → seat-level locking & concurrency
 * - BookingManager    → booking lifecycle (create, payment, cancel)
 *
 * This ensures each service has a single responsibility, while BookingService
 * acts as the facade that ties them together.
 *
 * RELATIONSHIP: Booking owns Payment (composition).
 * Payment does NOT store a bookingId — Booking already knows its own ID.
 * This avoids redundant data and makes the relationship clearer.
 *
 * LIFECYCLE: Call start() after construction to begin background tasks
 * (seat lock expiry scheduler). In production, this would be managed by
 * a DI container lifecycle (e.g., Spring @PostConstruct).
 */
public class BookingService {

    // Singleton instance
    private static BookingService instance;

    // Composed domain services
    private final UserService userService;
    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ShowService showService;
    private final SeatLockManager seatLockManager;
    private final BookingManager bookingManager;

    private boolean started = false;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private BookingService() {
        this.userService = UserService.getInstance();
        this.movieService =  MovieService.getInstance();
        this.theatreService =  TheatreService.getInstance();
        this.showService = ShowService.getInstance();
        this.seatLockManager =  SeatLockManager.getInstance();
        this.bookingManager =  BookingManager.getInstance();
    }

    /**
     * Constructor for dependency injection (testing, custom wiring).
     * Package-private to allow access within the same package for testing.
     */
 private   BookingService(UserService userService, MovieService movieService,
                    TheatreService theatreService, ShowService showService,
                    SeatLockManager seatLockManager, BookingManager bookingManager) {
        this.userService = userService;
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.showService = showService;
        this.seatLockManager = seatLockManager;
        this.bookingManager = bookingManager;
    }

    /**
     * Returns the singleton instance of BookingService.
     * The instance is initialized and started on first access.
     *
     * @return the singleton BookingService instance
     */
    public static synchronized BookingService getInstance() {
        if (instance == null) {
            instance = new BookingService();
            instance.start(); // Automatically start the singleton instance
        }
        return instance;
    }

    /**
     * Initializes background services (seat lock expiry scheduler).
     * Must be called once after construction before using the service.
     * Separating lifecycle from construction allows clean DI and testing.
     */
    public void start() {
        if (started) return;
        started = true;
        seatLockManager.startExpiryScheduler(
                () -> showService.getAllShows(), 30, TimeUnit.SECONDS);
    }

    // ==================== User Management (delegated to UserService) ====================

    public void addUser(User user) {
        userService.addUser(user);
    }

    public User getUser(String userId) {
        return userService.getUser(userId);
    }

    // ==================== Movie Management (delegated to MovieService) ====================

    public void addMovie(Movie movie) {
        movieService.addMovie(movie);
    }

    public Movie getMovie(String movieId) {
        return movieService.getMovie(movieId);
    }

    public List<Movie> searchMovies(String name) {
        return movieService.searchMovies(name);
    }

    // ==================== Theatre Management (delegated to TheatreService) ====================

    public void addTheatre(Theatre theatre) {
        theatreService.addTheatre(theatre);
    }

    public Theatre getTheatre(String theatreId) {
        return theatreService.getTheatre(theatreId);
    }

    public List<Theatre> searchTheatresByCity(String city) {
        return theatreService.searchTheatresByCity(city);
    }

    // ==================== Show Management (delegated to ShowService) ====================

    public void addShow(Show show,String theatreId) {
        showService.addShow(show,theatreId);
    }

    public Show getShow(String showId) {
        return showService.getShow(showId);
    }

    public List<Show> getShowsForMovie(String movieId) {
        return showService.getShowsForMovie(movieId);
    }

    public List<Show> getShowsForTheatre(String theatreId) {
        return showService.getShowsForTheatre(theatreId);
    }

    // ==================== ShowSeat Creation (delegated to BookingManager) ====================

    public void createShowSeats(Show show) {
        bookingManager.createShowSeats(show);
    }

    // ==================== Seat Availability (delegated to SeatLockManager) ====================

    public List<ShowSeat> showAvailableSeats(Show show) {
        // Release expired locks before showing available seats
        seatLockManager.releaseExpiredLocks(show.getShowSeats());
        return show.getShowSeats();
    }

    // ==================== Booking Creation (delegated to BookingManager) ====================

    public Booking createBooking(User user, Show show, List<String> seatIds) {
        return bookingManager.createBooking(user, show, seatIds);
    }

    // ==================== Payment (delegated to BookingManager) ====================

    public Payment makePayment(String bookingId) {
        return bookingManager.makePayment(bookingId);
    }

    // ==================== Cancellation (delegated to BookingManager) ====================

    public void cancelBooking(String bookingId) {
        bookingManager.cancelBooking(bookingId);
    }

    // ==================== Search (delegated to ShowService) ====================

    /**
     * Unified search for shows by city, movie title, and/or theatre name.
     * All parameters are optional — pass null or blank to skip that filter.
     * All matches are case-insensitive partial matches.
     */
    public List<Show> searchShows(String city, String movieTitle, String theatreName) {
        return showService.searchShows(city, movieTitle, theatreName);
    }

    // ==================== Query Methods ====================

    public Booking getBooking(String bookingId) {
        return bookingManager.getBooking(bookingId);
    }
}