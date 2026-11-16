package com.rocky.vendingmachine;

import com.rocky.vendingmachine.bank.CoinBank;
import com.rocky.vendingmachine.core.VendingMachine;
import com.rocky.vendingmachine.inventory.Inventory;
import com.rocky.vendingmachine.model.Coin;
import com.rocky.vendingmachine.model.Product;
import com.rocky.vendingmachine.payment.PaymentResult;

import java.util.List;

/**
 * Demo class to showcase all Vending Machine features.
 * Demonstrates: State Pattern, Strategy Pattern, Thread Safety, Multi-product cart, Refunds.
 */
public class VendingMachineDemo {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   VENDING MACHINE - LOW LEVEL DESIGN");
        System.out.println("==========================================\n");

        // Initialize inventory (Singleton)
        Inventory inventory = Inventory.getInstance();
        inventory.addProduct(new Product("P01", "Coke", 10.0), 5);
        inventory.addProduct(new Product("P02", "Pepsi", 15.0), 3);
        inventory.addProduct(new Product("P03", "Water", 5.0), 10);
        inventory.addProduct(new Product("P04", "Chips", 20.0), 4);
        inventory.addProduct(new Product("P05", "Chocolate", 25.0), 2);

        // Initialize coin bank (Singleton — defaults to 10 of each coin type)
        CoinBank coinBank = CoinBank.getInstance();

        // Create vending machine (Singleton)
        VendingMachine machine = VendingMachine.getInstance();

        System.out.println("=== PRODUCTS STOCKED ===");
        machine.printProductInventory();
        System.out.println("\n=== INITIAL COIN BANK ===");
        machine.printCoinInventory();

        // ==========================================
        // SCENARIO 1: Single product with COIN (exact amount)
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 1: COIN Payment - Single Product");
        System.out.println("===========================================");
        if (machine.acquireMachine()) {
            machine.displayProducts();
            machine.selectProduct("P03"); // Water - ₹5
            PaymentResult result = machine.processPayment("COIN", 5.0);
            System.out.println("Result: " + result.getMessage());
        }

        // ==========================================
        // SCENARIO 2: Multi-product cart with COIN (with change)
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 2: Multi-Product Cart - COIN with Change");
        System.out.println("===========================================");
        if (machine.acquireMachine()) {
            machine.displayProducts();
            machine.selectProduct("P01"); // Coke - ₹10
            machine.selectProduct("P04"); // Chips - ₹20 (total = ₹30)
            PaymentResult result = machine.processPayment("COIN", 50.0);
            System.out.println("Result: " + result.getMessage());
            if (result.getChangeAmount() > 0) {
                System.out.println("  Change coins: " + result.getChangeCoins());
            }
        }

        // ==========================================
        // SCENARIO 3: Multi-product cart with UPI
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 3: Multi-Product Cart - UPI");
        System.out.println("===========================================");
        if (machine.acquireMachine()) {
            machine.displayProducts();
            machine.selectProduct("P02"); // Pepsi - ₹15
            machine.selectProduct("P05"); // Chocolate - ₹25 (total = ₹40)
            PaymentResult result = machine.processPayment("UPI", 40.0);
            System.out.println("Result: " + result.getMessage());
        }

        // ==========================================
        // SCENARIO 4: Insufficient Payment - Auto Refund
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 4: Insufficient Payment - Auto Refund");
        System.out.println("===========================================");
        if (machine.acquireMachine()) {
            machine.displayProducts();
            machine.selectProduct("P02"); // Pepsi - ₹15
            PaymentResult result = machine.processPayment("COIN", 5.0);
            System.out.println("Result: " + result.getMessage());
        }

        // ==========================================
        // SCENARIO 5: Cancel Before Payment
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 5: Cancel Before Payment");
        System.out.println("===========================================");
        if (machine.acquireMachine()) {
            machine.displayProducts();
            machine.selectProduct("P05"); // Chocolate - ₹25
            System.out.println("[User] Changed mind, cancelling...");
            machine.cancelTransaction();
        }

        // ==========================================
        // SCENARIO 6: Cannot Make Change - Full Refund
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 6: Cannot Make Change - Full Refund");
        System.out.println("===========================================");
        System.out.println("(Buying items to drain small coins from bank...)");
        for (int i = 0; i < 5; i++) {
            if (machine.acquireMachine()) {
                machine.selectProduct("P03"); // Water - ₹5
                machine.processPayment("COIN", 5.0);
            }
        }

        System.out.println("\n[Test] Buying ₹5 item with ₹10 (expect change failure)...");
        if (machine.acquireMachine()) {
            machine.selectProduct("P03"); // Water - ₹5
            PaymentResult result = machine.processPayment("COIN", 10.0);
            System.out.println("Result: " + result.getMessage());
        }

        // ==========================================
        // SCENARIO 7: Thread Safety Test
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  SCENARIO 7: Thread Safety Test");
        System.out.println("===========================================");

        Thread user1 = new Thread(() -> {
            System.out.println("[User-1] Attempting to use machine...");
            if (machine.acquireMachine()) {
                System.out.println("[User-1] GOT THE MACHINE!");
                machine.selectProduct("P01"); // Coke - ₹10
                machine.processPayment("COIN", 10.0);
            } else {
                System.out.println("[User-1] Machine busy, will retry later...");
            }
        });

        Thread user2 = new Thread(() -> {
            System.out.println("[User-2] Attempting to use machine...");
            if (machine.acquireMachine()) {
                System.out.println("[User-2] GOT THE MACHINE!");
                machine.selectProduct("P01"); // Coke - ₹10
                PaymentResult result = machine.processPayment("COIN", 10.0);
                System.out.println("[User-2] " + result.getMessage());
            } else {
                System.out.println("[User-2] Machine busy, will retry later...");
            }
        });

        Thread user3 = new Thread(() -> {
            System.out.println("[User-3] Attempting to use machine...");
            if (machine.acquireMachine()) {
                System.out.println("[User-3] GOT THE MACHINE!");
                machine.selectProduct("P01"); // Coke - ₹10
                PaymentResult result = machine.processPayment("COIN", 10.0);
                System.out.println("[User-3] " + result.getMessage());
            } else {
                System.out.println("[User-3] Machine busy, will retry later...");
            }
        });

        user1.start();
        user2.start();
        user3.start();

        try {
            user1.join();
            user2.join();
            user3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ==========================================
        // FINAL STATE
        // ==========================================
        System.out.println("\n===========================================");
        System.out.println("  FINAL STATE");
        System.out.println("===========================================");
        machine.printProductInventory();
        System.out.println();
        machine.printCoinInventory();
        System.out.println();
        machine.printDigitalAccount();

        System.out.println("\n==========================================");
        System.out.println("  DEMO COMPLETE");
        System.out.println("==========================================");
    }
}