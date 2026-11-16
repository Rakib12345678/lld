package com.rocky.splitwise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group {
    private final String id;
    private final String name;
    private final List<User> members;

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(User user) {
        members.add(user);
    }

    @Override
    public String toString() {
        return "Group{id='" + id + "', name='" + name + "', members=" + members.size() + "}";
    }
}