package com.rocky.splitwise.service;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Group;
import com.rocky.splitwise.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GroupService {
    private final Map<String, Group> groups = new HashMap<>();
    private final UserService userService;

    public GroupService(UserService userService) {
        this.userService = userService;
    }

    public Group createGroup(String id, String name) {
        if (groups.containsKey(id)) {
            throw new SplitWiseException("Group already exists with id: " + id);
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        return group;
    }

    public void addMemberToGroup(String groupId, String userId) {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new SplitWiseException("Group not found: " + groupId);
        }
        User user = userService.getUser(userId);
        if (user == null) {
            throw new SplitWiseException("User not found: " + userId);
        }
        group.addMember(user);
    }

    public Group getGroup(String id) {
        return groups.get(id);
    }

    public Collection<Group> getAllGroups() {
        return groups.values();
    }
}