package com.rocky.vendingmachine.inventory;

import com.rocky.vendingmachine.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Manages product inventory for the vending machine.
 * Singleton — only one inventory instance for the entire machine.
 * Thread-safe using ConcurrentHashMap with atomic operations.
 */
public class Inventory {
    private static volatile Inventory instance;

    private final Map<String, Product> productCatalog;
    private final Map<String, Integer> stock;

    private Inventory() {
        this.productCatalog = new ConcurrentHashMap<>();
        this.stock = new ConcurrentHashMap<>();
    }

    /**
     * Returns the singleton instance of Inventory.
     * Double-checked locking for thread safety.
     */
    public static Inventory getInstance() {
        if (instance == null) {
            synchronized (Inventory.class) {
                if (instance == null) {
                    instance = new Inventory();
                }
            }
        }
        return instance;
    }

    /**
     * Adds a product to the catalog with initial stock.
     * Synchronized to ensure atomicity across both maps.
     */
    public synchronized void addProduct(Product product, int quantity) {
        productCatalog.put(product.getId(), product);
        stock.merge(product.getId(), quantity, Integer::sum);
    }

    /**
     * Checks if a product is available in stock.
     */
    public boolean isAvailable(String productId) {
        return stock.getOrDefault(productId, 0) > 0;
    }

    /**
     * Gets a product by ID.
     */
    public Product getProduct(String productId) {
        return productCatalog.get(productId);
    }

    /**
     * Atomically dispenses one unit of the product (reduces stock).
     * Uses ConcurrentHashMap.computeIfPresent for atomic check-and-act.
     */
    public Product dispense(String productId) {
        AtomicReference<Product> dispensed = new AtomicReference<>(null);
        stock.computeIfPresent(productId, (id, remaining) -> {
            if (remaining <= 0) {
                return remaining; // No change — out of stock
            }
            dispensed.set(productCatalog.get(id));
            return remaining - 1;
        });
        return dispensed.get();
    }

    /**
     * Returns a list of all available products with stock > 0.
     * Uses a snapshot-style iteration (weakly consistent, acceptable for display).
     */
    public List<Product> getAvailableProducts() {
        return stock.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> productCatalog.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Prints the current inventory status.
     */
    public void printInventory() {
        System.out.println("=== Product Inventory ===");
        for (Map.Entry<String, Integer> entry : stock.entrySet()) {
            Product product = productCatalog.get(entry.getKey());
            if (product != null) {
                System.out.println(product.getName() + " (ID: " + entry.getKey()
                        + ") - ₹" + product.getPrice()
                        + " | Stock: " + entry.getValue());
            }
        }
        System.out.println("=========================");
    }
}