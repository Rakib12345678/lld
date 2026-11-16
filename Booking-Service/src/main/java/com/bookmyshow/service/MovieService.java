package com.bookmyshow.service;

import com.bookmyshow.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MovieService manages all movie-related operations.
 * <p>
 * Responsibilities:
 * - Adding new movies with duplicate validation
 * - Retrieving movies by ID
 * - Searching movies by title (case-insensitive partial match)
 * <p>
 * This follows the Single Responsibility Principle — MovieService
 * only knows about Movies and nothing else.
 */
public class MovieService {
  private static final MovieService movieService=new MovieService();
    private final Map<String, Movie> movies = new HashMap<>();
    private MovieService(){};

    public void addMovie(Movie movie) {
        if (movies.containsKey(movie.getId())) {
            throw new IllegalArgumentException("Movie already exists: " + movie.getId());
        }
        movies.put(movie.getId(), movie);
    }

    public Movie getMovie(String movieId) {
        Movie movie = movies.get(movieId);
        if (movie == null) {
            throw new IllegalArgumentException("Movie not found: " + movieId);
        }
        return movie;
    }

    public List<Movie> searchMovies(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>(movies.values());
        }
        return movies.values().stream()
                .filter(m -> m.getTitle().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public synchronized static MovieService getInstance(){
        return movieService;
    }
}