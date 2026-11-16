package com.bookmyshow.service;

import com.bookmyshow.model.Show;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ShowService manages all show-related operations.
 * <p>
 * Responsibilities:
 * - Adding new shows with duplicate validation
 * - Retrieving shows by ID
 * - Advanced show search by city, movie title, and/or theatre name
 * - Querying shows for a given movie or theatre
 * <p>
 * This follows the Single Responsibility Principle — ShowService
 * only knows about Shows. It depends on TheatreService for theatre
 * lookups when performing cross-entity search.
 */



public class ShowService {

    private final Map<String, Show> shows = new HashMap<>();
    private final Map<String, List<Show>> theatreShows = new HashMap<>();

    private final TheatreService theatreService;

    private static final ShowService INSTANCE = new ShowService();

    private ShowService() {
        this.theatreService = TheatreService.getInstance();
    }

    public static ShowService getInstance() {
        return INSTANCE;
    }

    public void addShow(Show show, String theatreId) {
        if (shows.containsKey(show.getId())) {
            throw new IllegalArgumentException(
                    "Show already exists: " + show.getId()
            );
        }

        shows.put(show.getId(), show);

        theatreShows
                .computeIfAbsent(theatreId, k -> new ArrayList<>())
                .add(show);
    }

    public Show getShow(String showId) {
        Show show = shows.get(showId);

        if (show == null) {
            throw new IllegalArgumentException(
                    "Show not found: " + showId
            );
        }

        return show;
    }

    public List<Show> getShowsForMovie(String movieId) {
        return shows.values().stream()
                .filter(show ->
                        show.getMovie().getId().equals(movieId))
                .collect(Collectors.toList());
    }

    public List<Show> getShowsForTheatre(String theatreId) {
        return Collections.unmodifiableList(
                theatreShows.getOrDefault(
                        theatreId,
                        Collections.emptyList()
                )
        );
    }

    public List<Show> getAllShows() {
        return Collections.unmodifiableList(
                new ArrayList<>(shows.values())
        );
    }

    public List<Show> searchShows(
            String city,
            String movieTitle,
            String theatreName) {

        Stream<Show> stream = shows.values().stream();

        if (city != null && !city.isBlank()) {
            Set<String> screenIds =
                    theatreService.getScreenIdsByCity(city);

            if (screenIds.isEmpty()) {
                return Collections.emptyList();
            }

            stream = stream.filter(show ->
                    screenIds.contains(
                            show.getScreen().getId()
                    ));
        }

        if (movieTitle != null && !movieTitle.isBlank()) {
            String title = movieTitle.toLowerCase();

            stream = stream.filter(show ->
                    show.getMovie()
                            .getTitle()
                            .toLowerCase()
                            .contains(title));
        }

        if (theatreName != null && !theatreName.isBlank()) {
            Set<String> screenIds =
                    theatreService.getScreenIdsByTheatreName(theatreName);

            if (screenIds.isEmpty()) {
                return Collections.emptyList();
            }

            stream = stream.filter(show ->
                    screenIds.contains(
                            show.getScreen().getId()
                    ));
        }

        return stream.collect(Collectors.toList());
    }
}