package com.rocky.vendingmachine.bank;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a digital account/wallet where UPI payments are stored.
 * Singleton — only one digital account for the entire machine.
 * In a real system, this would connect to a payment gateway (Razorpay, Paytm, etc.).
 * Thread-safe using AtomicReference.
 */
public class DigitalAccount {
    private static volatile DigitalAccount instance;

    private final AtomicReference<Double> balance;
    private final String accountId;

    private DigitalAccount(String accountId) {
        this.accountId = accountId;
        this.balance = new AtomicReference<>(0.0);
    }

    /**
     * Returns the singleton instance of DigitalAccount.
     * Double-checked locking for thread safety.
     */
    public static DigitalAccount getInstance() {
        if (instance == null) {
            synchronized (DigitalAccount.class) {
                if (instance == null) {
                    instance = new DigitalAccount("VENDING-MERCHANT-001");
                }
            }
        }
        return instance;
    }

    /**
     * Credits (deposits) money into the digital account.
     * Called when a UPI payment is successfully processed.
     */
    public double credit(double amount) {
        while (true) {
            Double current = balance.get();
            Double newBalance = current + amount;
            if (balance.compareAndSet(current, newBalance)) {
                System.out.println("[DigitalAccount:" + accountId + "] Credited ₹" + amount
                        + ". Balance: ₹" + newBalance);
                return newBalance;
            }
        }
    }

    /**
     * Returns the current balance.
     */
    public double getBalance() {
        return balance.get();
    }

    /**
     * Prints account statement.
     */
    public void printStatement() {
        System.out.println("=== Digital Account ===");
        System.out.println("Account ID: " + accountId);
        System.out.println("Balance: ₹" + String.format("%.2f", balance.get()));
        System.out.println("=======================");
    }
}