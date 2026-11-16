package com.rocky.splitwise.service;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private final Map<String, User> users = new HashMap<>();

    public User addUser(String id, String name, String email, String phone) {
        if (users.containsKey(id)) {
            throw new SplitWiseException("User already exists with id: " + id);
        }
        User user = new User(id, name, email, phone);
        users.put(id, user);
        return user;
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public boolean userExists(String id) {
        return users.containsKey(id);
    }
}