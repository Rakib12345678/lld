package com.rocky.vendingmachine.bank;

import com.rocky.vendingmachine.model.Coin;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Manages coin inventory for the vending machine.
 * Singleton — only one coin bank for the entire machine.
 * Handles depositing coins, making change, and refunding coins.
 * Thread-safe using ReentrantLock.
 */
public class CoinBank {
    private static volatile CoinBank instance;

    private final Map<Coin, Integer> coinInventory;
    private final ReentrantLock lock = new ReentrantLock();

    private CoinBank() {
        this.coinInventory = new HashMap<>();
        // Initialize with some coins for change-making
        for (Coin coin : Coin.values()) {
            coinInventory.put(coin, 10); // 10 of each coin type initially
        }
    }

    /**
     * Returns the singleton instance of CoinBank.
     * Double-checked locking for thread safety.
     */
    public static CoinBank getInstance() {
        if (instance == null) {
            synchronized (CoinBank.class) {
                if (instance == null) {
                    instance = new CoinBank();
                }
            }
        }
        return instance;
    }

    /**
     * Deposits a list of coins into the bank.
     */
    public void depositCoins(List<Coin> coins) {
        lock.lock();
        try {
            for (Coin coin : coins) {
                coinInventory.merge(coin, 1, Integer::sum);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to withdraw exact change for the given amount.
     * Uses a greedy algorithm with available coins.
     * Returns null if exact change cannot be made.
     */
    public List<Coin> withdrawChange(double amount) {
        lock.lock();
        try {
            int amountInRupees = (int) Math.round(amount);
            if (amountInRupees <= 0) {
                return Collections.emptyList();
            }

            // Greedy: try to use highest denominations first
            List<Coin> sortedCoins = Arrays.stream(Coin.values())
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList());

            List<Coin> result = new ArrayList<>();
            Map<Coin, Integer> tempDeduction = new HashMap<>();
            int remaining = amountInRupees;

            for (Coin coin : sortedCoins) {
                int value = coin.getValue();
                int available = coinInventory.getOrDefault(coin, 0);
                int needed = remaining / value;
                int toUse = Math.min(needed, available);

                if (toUse > 0) {
                    tempDeduction.put(coin, toUse);
                    remaining -= toUse * value;
                }
            }

            if (remaining != 0) {
                return null; // Cannot make exact change
            }

            // Apply deductions
            for (Map.Entry<Coin, Integer> entry : tempDeduction.entrySet()) {
                coinInventory.merge(entry.getKey(), -entry.getValue(), Integer::sum);
                for (int i = 0; i < entry.getValue(); i++) {
                    result.add(entry.getKey());
                }
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Refunds a given amount in coins.
     * This is used when a transaction is cancelled or fails.
     */
    public List<Coin> refundCoins(double amount) {
        return withdrawChange(amount);
    }

    /**
     * Gets total coin value currently in bank.
     */
    public double getTotalValue() {
        lock.lock();
        try {
            return coinInventory.entrySet().stream()
                    .mapToDouble(e -> e.getKey().getValue() * e.getValue())
                    .sum();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Prints current coin inventory.
     */
    public void printInventory() {
        lock.lock();
        try {
            System.out.println("=== Coin Bank Inventory ===");
            for (Coin coin : Coin.values()) {
                System.out.println(coin + ": " + coinInventory.getOrDefault(coin, 0) + " coins");
            }
            System.out.println("Total value: ₹" + getTotalValue());
            System.out.println("==========================");
        } finally {
            lock.unlock();
        }
    }
}