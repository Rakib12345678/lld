package com.rocky.splitwise.model;

public class Settlement {
    private final String id;
    private final User paidBy;
    private final User receivedBy;
    private final double amount;

    public Settlement(String id, User paidBy, User receivedBy, double amount) {
        this.id = id;
        this.paidBy = paidBy;
        this.receivedBy = receivedBy;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public User getReceivedBy() {
        return receivedBy;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Settlement{id='" + id + "', from=" + paidBy.getName() +
                ", to=" + receivedBy.getName() + ", amount=" + amount + "}";
    }
}