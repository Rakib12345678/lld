package com.bookmyshow.service;

import com.bookmyshow.model.Screen;
import com.bookmyshow.model.Theatre;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TheatreService manages all theatre-related operations.
 * <p>
 * Responsibilities:
 * - Adding new theatres with duplicate validation
 * - Retrieving theatres by ID
 * - Searching theatres by city (case-insensitive)
 * - Providing theatre/screen lookups for show searching
 * <p>
 * This follows the Single Responsibility Principle — TheatreService
 * only knows about Theatres and nothing else.
 */
public class TheatreService {

    private final Map<String, Theatre> theatres = new HashMap<>();
    private final Map<String, List<Theatre>> theatresByCity = new HashMap<>();
    private static final TheatreService INSTANCE = new TheatreService();
    private TheatreService(){}
    public void addTheatre(Theatre theatre) {
        if (theatres.containsKey(theatre.getId())) {
            throw new IllegalArgumentException("Theatre already exists: " + theatre.getId());
        }
        theatres.put(theatre.getId(), theatre);
        theatresByCity.computeIfAbsent(theatre.getCity().toLowerCase(), k -> new ArrayList<>()).add(theatre);
    }

    public Theatre getTheatre(String theatreId) {
        Theatre theatre = theatres.get(theatreId);
        if (theatre == null) {
            throw new IllegalArgumentException("Theatre not found: " + theatreId);
        }
        return theatre;
    }

    public List<Theatre> searchTheatresByCity(String city) {
        if (city == null || city.isBlank()) {
            return new ArrayList<>(theatres.values());
        }
        return theatresByCity.getOrDefault(city.toLowerCase(), Collections.emptyList());
    }

    /**
     * Returns all screen IDs that belong to theatres in the given city.
     * Returns empty set if city is null/blank or no theatres found.
     */
    public Set<String> getScreenIdsByCity(String city) {
        if (city == null || city.isBlank()) {
            return Collections.emptySet();
        }
        List<Theatre> cityTheatres = theatresByCity.get(city.toLowerCase());
        if (cityTheatres == null || cityTheatres.isEmpty()) {
            return Collections.emptySet();
        }
        return cityTheatres.stream()
                .flatMap(t -> t.getScreens().stream())
                .map(Screen::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns all screen IDs that belong to theatres matching the given name (partial, case-insensitive).
     * Returns empty set if theatreName is null/blank.
     */
    public Set<String> getScreenIdsByTheatreName(String theatreName) {
        if (theatreName == null || theatreName.isBlank()) {
            return Collections.emptySet();
        }
        String lowerName = theatreName.toLowerCase();
        return theatres.values().stream()
                .filter(t -> t.getName().toLowerCase().contains(lowerName))
                .flatMap(t -> t.getScreens().stream())
                .map(Screen::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns all screen IDs for a specific theatre by ID.
     * Returns empty set if theatreId is null/blank or theatre not found.
     */
    public Set<String> getScreenIdsByTheatreId(String theatreId) {
        if (theatreId == null || theatreId.isBlank()) {
            return Collections.emptySet();
        }
        Theatre theatre = theatres.get(theatreId);
        if (theatre == null) {
            return Collections.emptySet();
        }
        return theatre.getScreens().stream()
                .map(Screen::getId)
                .collect(Collectors.toSet());
    }

    public static TheatreService getInstance(){
        return INSTANCE;
    }
}
