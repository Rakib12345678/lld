package com.rocky.vendingmachine.payment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and managing payment strategies.
 * Encapsulates strategy registration and lookup.
 * Thread-safe using ConcurrentHashMap.
 *
 * Open for extension (via registerStrategy) — closed for modification.
 * Adding a new payment method requires only a registerStrategy() call externally.
 */
public class PaymentStrategyFactory {
    private static volatile PaymentStrategyFactory instance;

    private final Map<String, PaymentStrategy> strategies;

    private PaymentStrategyFactory() {
        this.strategies = new ConcurrentHashMap<>();
    }

    /**
     * Returns the singleton instance of PaymentStrategyFactory.
     */
    public static PaymentStrategyFactory getInstance() {
        if (instance == null) {
            synchronized (PaymentStrategyFactory.class) {
                if (instance == null) {
                    instance = new PaymentStrategyFactory();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a payment strategy for a given method name.
     */
    public void registerStrategy(String method, PaymentStrategy strategy) {
        strategies.put(method.toUpperCase(), strategy);
    }

    /**
     * Returns the payment strategy for the given method.
     * @param method payment method (e.g., "COIN", "UPI")
     * @return PaymentStrategy, or null if not found
     */
    public PaymentStrategy getStrategy(String method) {
        return strategies.get(method.toUpperCase());
    }

    /**
     * Returns all available payment methods.
     */
    public String getAvailableMethods() {
        return String.join(", ", strategies.keySet());
    }
}