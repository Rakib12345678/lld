package com.bookmyshow;

import com.bookmyshow.enums.BookingStatus;
import com.bookmyshow.enums.SeatType;
import com.bookmyshow.model.*;
import com.bookmyshow.service.BookingService;
import com.bookmyshow.strategy.DefaultPricingStrategy;
import com.bookmyshow.strategy.PricingStrategy;
import com.bookmyshow.strategy.WeekendPricingStrategy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BookMyShow LLD Demo — Interview-oriented Main class.
 * Demonstrates the complete booking flow end-to-end.
 */
public class Main {
    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("   BOOKMYSHOW LLD DEMO");
        System.out.println("========================================\n");

        // ==================== 1. Create BookingService Instance ====================
        BookingService bookingService = BookingService.getInstance();

        // ==================== 2. Create Users ====================
        User user1 = new User("u1", "Alice", "alice@email.com");
        User user2 = new User("u2", "Bob", "bob@email.com");
        bookingService.addUser(user1);
        bookingService.addUser(user2);
        System.out.println("Users created: " + user1.getName() + ", " + user2.getName());

        // ==================== 3. Create Movies ====================
        Movie movie1 = new Movie("m1", "Inception", "Sci-Fi", 148);
        Movie movie2 = new Movie("m2", "Interstellar", "Sci-Fi", 169);
        bookingService.addMovie(movie1);
        bookingService.addMovie(movie2);
        System.out.println("Movies created: " + movie1.getTitle() + ", " + movie2.getTitle());

        // ==================== 4. Create Theatres ====================
        Theatre theatre1 = new Theatre("t1", "PVR Cinemas", "Mumbai");
        Theatre theatre2 = new Theatre("t2", "INOX", "Mumbai");
        Theatre theatre3 = new Theatre("t3", "Cinepolis", "Delhi");
        bookingService.addTheatre(theatre1);
        bookingService.addTheatre(theatre2);
        bookingService.addTheatre(theatre3);
        System.out.println("Theatres created: " + theatre1.getName() + " (" + theatre1.getCity() + "), "
                + theatre2.getName() + " (" + theatre2.getCity() + "), "
                + theatre3.getName() + " (" + theatre3.getCity() + ")");

        // ==================== 6. Create Screens ====================
        Screen screen1 = new Screen("s1", "Screen 1");
        Screen screen2 = new Screen("s2", "Screen 2");

        // ==================== 6. Create Seats ====================
        // Screen 1: A1(REGULAR), A2(REGULAR), A3(PREMIUM), A4(RECLINER)
        Seat a1 = new Seat("a1", "A1", SeatType.REGULAR);
        Seat a2 = new Seat("a2", "A2", SeatType.REGULAR);
        Seat a3 = new Seat("a3", "A3", SeatType.PREMIUM);
        Seat a4 = new Seat("a4", "A4", SeatType.RECLINER);

        screen1.addSeat(a1);
        screen1.addSeat(a2);
        screen1.addSeat(a3);
        screen1.addSeat(a4);

        // Screen 2: B1(REGULAR), B2(PREMIUM)
        Seat b1 = new Seat("b1", "B1", SeatType.REGULAR);
        Seat b2 = new Seat("b2", "B2", SeatType.PREMIUM);
        screen2.addSeat(b1);
        screen2.addSeat(b2);

        System.out.println("Screens and seats created.");

        // ==================== 7. Add Screens to Theatres ====================
        theatre1.addScreen(screen1);
        theatre1.addScreen(screen2);
        System.out.println("Screens added to " + theatre1.getName());

        // ==================== 8. Create Show ====================
        Show show1 = new Show("sh1", movie1, screen1, LocalDateTime.of(2026,9,12,5,30));
        bookingService.addShow(show1,theatre1.getId());
        System.out.println("Show created: " + movie1.getTitle() + " at " + theatre1.getName() + " - " + screen1.getName());

        // ==================== 9. Create ShowSeats using PricingStrategy ====================
        bookingService.createShowSeats(show1);

        // ==================== 10. Demonstrate Search Features ====================
        System.out.println("\n===== SEARCH DEMO =====");

        // Search theatres by city
        System.out.println("\n--- Search theatres by city 'Mumbai' ---");
        List<Theatre> mumbaiTheatres = bookingService.searchTheatresByCity("Mumbai");
        for (Theatre t : mumbaiTheatres) {
            System.out.println(t.getName() + " (" + t.getCity() + ")");
        }

        // Search movies by title
        System.out.println("\n--- Search movies by title 'inception' ---");
        List<Movie> foundMovies = bookingService.searchMovies("inception");
        for (Movie m : foundMovies) {
            System.out.println(m.getTitle() + " (" + m.getGenre() + ")");
        }

        // Search shows by movie title only
        System.out.println("\n--- Search shows by movie title 'inception' ---");
        List<Show> foundShows = bookingService.searchShows(null, "inception", null);
        for (Show s : foundShows) {
            System.out.println(s.getMovie().getTitle() + " at " + s.getStartTime());
        }

        // Search shows by city and movie title
        System.out.println("\n--- Search shows by city 'Mumbai' and movie 'inception' ---");
        List<Show> cityMovieShows = bookingService.searchShows("Mumbai", "inception", null);
        for (Show s : cityMovieShows) {
            System.out.println(s.getMovie().getTitle() + " at " + s.getStartTime());
        }

        // Search shows by theatre name
        System.out.println("\n--- Search shows by theatre name 'PVR' ---");
        List<Show> theatreShows = bookingService.searchShows(null, null, "PVR");
        for (Show s : theatreShows) {
            System.out.println(s.getMovie().getTitle() + " at " + s.getStartTime());
        }

        // ==================== 11. Display Available Seats ====================
        System.out.println("\n===== AVAILABLE SEATS =====");
        List<ShowSeat> availableSeats = bookingService.showAvailableSeats(show1);
        for (ShowSeat ss : availableSeats) {
            System.out.println(ss.getSeat().getLabel() + " -> " + ss.getStatus() + " -> ₹" + (int) ss.getPrice());
        }

        // ==================== 11. User 1: Book A1 and A2 ====================
        System.out.println("\n===== USER 1 (Alice) BOOKING =====");
        try {
            Booking booking1 = bookingService.createBooking(user1, show1, List.of("A1", "A2"));
            System.out.println("Seats locked: A1, A2");
            System.out.println("Booking created with ID: " + booking1.getId());
            System.out.println("Amount: ₹" + (int) booking1.getAmount());
            System.out.println("Status: " + booking1.getStatus());

            // 12. Make payment
            bookingService.makePayment(booking1.getId());
            System.out.println("Payment successful");
            System.out.println("Booking confirmed! Status: " + bookingService.getBooking(booking1.getId()).getStatus());

            // ==================== 13. Display Seats After Booking ====================
            System.out.println("\n===== SEATS AFTER BOOKING =====");
            for (ShowSeat ss : bookingService.showAvailableSeats(show1)) {
                System.out.println(ss.getSeat().getLabel() + " -> " + ss.getStatus() + " -> ₹" + (int) ss.getPrice());
            }

            // ==================== 14. User 2: Try to book A1 and A3 (A1 is already BOOKED) ====================
            System.out.println("\n===== USER 2 (Bob) BOOKING =====");
            try {
                bookingService.createBooking(user2, show1, List.of("A1", "A3"));
                System.out.println("ERROR: This should not have succeeded!");
            } catch (IllegalStateException e) {
                System.out.println("Seat A1 is not available");
                System.out.println("Booking failed: " + e.getMessage());
                System.out.println("Double booking prevented!");
            }

            // ==================== 15. User 1 Cancels Booking ====================
            System.out.println("\n===== CANCELLATION =====");
            bookingService.cancelBooking(booking1.getId());
            System.out.println("Booking cancelled");
            System.out.println("Status: " + bookingService.getBooking(booking1.getId()).getStatus());

            // Show available seats after cancellation
            System.out.println("\n===== SEATS AFTER CANCELLATION =====");
            for (ShowSeat ss : bookingService.showAvailableSeats(show1)) {
                System.out.println(ss.getSeat().getLabel() + " -> " + ss.getStatus() + " -> ₹" + (int) ss.getPrice());
            }

            // ==================== 16. User 2: Book A1 and A3 (now available) ====================
            System.out.println("\n===== USER 2 (Bob) BOOKING AGAIN =====");
            Booking booking2 = bookingService.createBooking(user2, show1, List.of("A1", "A3"));
            System.out.println("Seats locked: A1, A3");
            System.out.println("Booking created with ID: " + booking2.getId());
            System.out.println("Amount: ₹" + (int) booking2.getAmount());

            bookingService.makePayment(booking2.getId());
            System.out.println("Payment successful");
            System.out.println("Booking confirmed! Status: " + bookingService.getBooking(booking2.getId()).getStatus());

            // Final seat state
            System.out.println("\n===== FINAL SEAT STATE =====");
            for (ShowSeat ss : bookingService.showAvailableSeats(show1)) {
                System.out.println(ss.getSeat().getLabel() + " -> " + ss.getStatus() + " -> ₹" + (int) ss.getPrice());
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================");
        System.out.println("   DEMO COMPLETED SUCCESSFULLY");
        System.out.println("========================================");
    }
}