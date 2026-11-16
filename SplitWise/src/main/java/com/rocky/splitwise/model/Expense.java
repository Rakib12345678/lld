package com.rocky.splitwise.model;

import java.util.List;

public class Expense {
    private final String id;
    private final String description;
    private final double amount;
    private final User paidBy;
    private final List<User> participants;
    private final SplitType splitType;
    private final List<Double> splitValues; // exact amounts or percentages (null for EQUAL)
    private final String groupId; // optional - null for direct (non-group) expenses

    public Expense(String id, String description, double amount, User paidBy,
                   List<User> participants, SplitType splitType, List<Double> splitValues,
                   String groupId) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.participants = participants;
        this.splitType = splitType;
        this.splitValues = splitValues;
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<Double> getSplitValues() {
        return splitValues;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "Expense{id='" + id + "', desc='" + description + "', amount=" + amount +
                ", paidBy=" + paidBy.getName() + ", type=" + splitType +
                (groupId != null ? ", group=" + groupId : "") + "}";
    }
}