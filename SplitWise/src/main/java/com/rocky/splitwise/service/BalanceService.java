package com.rocky.splitwise.service;

import com.rocky.splitwise.model.User;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BalanceService {
    // Balance[u1][u2] = amount u1 owes u2 (positive means u1 owes u2, negative means u2 owes u1)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> balances = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Updates balance when user1 owes amount to user2.
     * Thread-safe: uses a lock to ensure atomicity of pairwise balance updates.
     */
    public void addOwing(User fromUser, User toUser, double amount) {
        lock.lock();
        try {
            double currentBalance = getBalanceInternal(fromUser, toUser);
            double newBalance = currentBalance + amount;
            setBalanceInternal(fromUser, toUser, newBalance);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reverses the direction: toUser receives money from fromUser.
     * Same as addOwing from the perspective of net balance.
     */
    public void receiveOwing(User fromUser, User toUser, double amount) {
        addOwing(fromUser, toUser, -amount);
    }

    /**
     * Gets the net balance from user1 to user2.
     * Positive means user1 owes user2, negative means user2 owes user1.
     */
    public double getBalance(User user1, User user2) {
        return getBalanceInternal(user1, user2);
    }

    private double getBalanceInternal(User user1, User user2) {
        Map<String, Double> user1Balances = balances.get(user1.getId());
        if (user1Balances == null) return 0.0;
        return user1Balances.getOrDefault(user2.getId(), 0.0);
    }

    private void setBalanceInternal(User user1, User user2, double amount) {
        balances.computeIfAbsent(user1.getId(), k -> new ConcurrentHashMap<>())
                .put(user2.getId(), Math.round(amount * 100.0) / 100.0);
        // Maintain reverse balance: ensure consistency
        balances.computeIfAbsent(user2.getId(), k -> new ConcurrentHashMap<>())
                .put(user1.getId(), Math.round(-amount * 100.0) / 100.0);
    }

    /**
     * Returns a read-only view of all balances.
     */
    public Map<String, Map<String, Double>> getAllBalances() {
        Map<String, Map<String, Double>> snapshot = new ConcurrentHashMap<>();
        for (Map.Entry<String, ConcurrentHashMap<String, Double>> entry : balances.entrySet()) {
            snapshot.put(entry.getKey(), Collections.unmodifiableMap(new ConcurrentHashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Prints a summary of all pairwise net balances.
     */
    public void printBalances(UserService userService) {
        System.out.println("\n========== BALANCE SUMMARY ==========");
        boolean hasBalances = false;
        for (Map.Entry<String, ConcurrentHashMap<String, Double>> entry : balances.entrySet()) {
            String user1Id = entry.getKey();
            User user1 = userService.getUser(user1Id);
            if (user1 == null) continue;

            for (Map.Entry<String, Double> balanceEntry : entry.getValue().entrySet()) {
                String user2Id = balanceEntry.getKey();
                double amount = balanceEntry.getValue();
                if (Math.abs(amount) > 0.01 && user1Id.compareTo(user2Id) < 0) {
                    User user2 = userService.getUser(user2Id);
                    if (user2 == null) continue;
                    hasBalances = true;
                    if (amount > 0) {
                        System.out.println("  " + user1.getName() + " owes " + user2.getName()
                                + " $" + String.format("%.2f", amount));
                    } else {
                        System.out.println("  " + user2.getName() + " owes " + user1.getName()
                                + " $" + String.format("%.2f", Math.abs(amount)));
                    }
                }
            }
        }
        if (!hasBalances) {
            System.out.println("  No outstanding balances.");
        }
        System.out.println("=====================================\n");
    }

    public void clear() {
        lock.lock();
        try {
            balances.clear();
        } finally {
            lock.unlock();
        }
    }
}