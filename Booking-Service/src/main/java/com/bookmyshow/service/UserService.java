package com.bookmyshow.service;

import com.bookmyshow.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * UserService manages all user-related operations.
 * <p>
 * Responsibilities:
 * - Adding new users with duplicate validation
 * - Retrieving users by ID
 * <p>
 * This follows the Single Responsibility Principle — UserService
 * only knows about Users and nothing else.
 */
public class UserService {
    private final Map<String, User> users = new HashMap<>();

    private static final UserService INSTANCE = new UserService();

    private UserService() {
    }

    public static UserService getInstance() {
        return INSTANCE;
    }

    public void addUser(User user) {
        if (users.containsKey(user.getId())) {
            throw new IllegalArgumentException("User already exists: " + user.getId());
        }
        users.put(user.getId(), user);
    }

    public User getUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }
}