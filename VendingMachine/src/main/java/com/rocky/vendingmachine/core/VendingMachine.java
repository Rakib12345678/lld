package com.rocky.vendingmachine.core;

import com.rocky.vendingmachine.bank.CoinBank;
import com.rocky.vendingmachine.bank.DigitalAccount;
import com.rocky.vendingmachine.inventory.Inventory;
import com.rocky.vendingmachine.model.Coin;
import com.rocky.vendingmachine.model.Product;
import com.rocky.vendingmachine.payment.*;
import com.rocky.vendingmachine.state.*;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Central Vending Machine controller — Singleton.
 * Uses **State Design Pattern** for transaction state management.
 * Uses **Strategy Design Pattern** for payment processing.
 * Thread-safe via Semaphore(1) — only one user at a time.
 * Full refund support on cancel or payment failure.
 * Supports multi-product cart.
 */
public class VendingMachine {
    private static volatile VendingMachine instance;

    private final Inventory inventory;
    private final CoinBank coinBank;
    private final DigitalAccount digitalAccount;
    private final Semaphore accessLock = new Semaphore(1);
    private final PaymentStrategyFactory strategyFactory;

    // Current state — dynamically changes as the transaction progresses
    private VendingMachineState currentState;

    // Transaction context (per user session)
    private final List<Product> cart = new ArrayList<>();
    private double amountPaid;
    private String paymentMethod;
    private String payerId;

    private VendingMachine() {
        this.inventory = Inventory.getInstance();
        this.coinBank = CoinBank.getInstance();
        this.digitalAccount = DigitalAccount.getInstance();
        this.strategyFactory = PaymentStrategyFactory.getInstance();

        // Register default payment strategies (OCP: add new ones here, not in Factory)
        strategyFactory.registerStrategy("COIN", new CoinPaymentStrategy());
        strategyFactory.registerStrategy("UPI", new UPIPaymentStrategy());

        // Start in idle state
        this.currentState = new IdleState(this);
    }

    /**
     * Returns the singleton instance of VendingMachine.
     * Double-checked locking for thread safety.
     */
    public static VendingMachine getInstance() {
        if (instance == null) {
            synchronized (VendingMachine.class) {
                if (instance == null) {
                    instance = new VendingMachine();
                }
            }
        }
        return instance;
    }

    // ========== PUBLIC API ==========

    /**
     * Acquire the machine exclusively for one user.
     * Thread-safe via Semaphore.tryAcquire().
     */
    public boolean acquireMachine() {
        if (accessLock.tryAcquire()) {
            resetTransaction();
            setState(new ProductSelectionState(this));
            System.out.println("[VendingMachine] Machine acquired by user.");
            System.out.println("[VendingMachine] State → PRODUCT_SELECTION");
            return true;
        }
        System.out.println("[VendingMachine] Machine is busy. Please wait.");
        return false;
    }

    /**
     * Display all available products.
     */
    public void displayProducts() {
        List<Product> products = inventory.getAvailableProducts();
        if (products.isEmpty()) {
            System.out.println("No products available. Please restock.");
            return;
        }
        System.out.println("\n=== Available Products ===");
        for (Product p : products) {
            System.out.println("  [" + p.getId() + "] " + p.getName() + " - ₹" + p.getPrice());
        }
        System.out.println("==========================\n");
    }

    /**
     * Select a product — delegates to current state.
     */
    public boolean selectProduct(String productId) {
        return currentState.selectProduct(productId);
    }

    /**
     * Process payment — delegates to current state.
     */
    public PaymentResult processPayment(String method, double amountPaid) {
        return currentState.processPayment(method, amountPaid);
    }

    /**
     * Cancel the current transaction at the machine level.
     */
    public void cancelTransaction() {
        if (currentState instanceof IdleState) {
            System.out.println("[VendingMachine] No active transaction to cancel.");
            return;
        }

        System.out.println("[VendingMachine] Cancelling transaction in state: " + currentState.getStateName());

        if (currentState instanceof DispensingState) {
            System.out.println("[VendingMachine] Product was being dispensed. Cannot cancel at this stage.");
            return;
        }

        if (currentState instanceof PaymentPendingState && amountPaid > 0) {
            System.out.println("[VendingMachine] Refunding ₹" + amountPaid + "...");
            coinBank.refundCoins(amountPaid);
        }

        if (!cart.isEmpty()) {
            System.out.println("[VendingMachine] Cancelled order for: " + cart.size() + " product(s)");
        }

        refundAndCancel();
        setState(new IdleState(this));
        System.out.println("[VendingMachine] Machine is now available for next user.");
    }

    /**
     * Dispense product — delegates to current state.
     */
    public boolean dispenseProduct() {
        return currentState.dispenseProduct();
    }

    // ========== STATE TRANSITION ==========

    public void setState(VendingMachineState newState) {
        this.currentState = newState;
    }

    // ========== STATE METHODS (called by State objects) ==========

    /**
     * Called by ProductSelectionState to add a product to the cart.
     * User can add multiple products before proceeding to payment.
     */
    public boolean selectProductInState(String productId) {
        if (!inventory.isAvailable(productId)) {
            System.out.println("[VendingMachine] Product not available: " + productId);
            return false;
        }
        Product product = inventory.getProduct(productId);
        cart.add(product);
        System.out.println("[VendingMachine] Added to cart: " + product.getName() + " - ₹" + product.getPrice());
        System.out.println("[VendingMachine] Cart total: ₹" + getCartTotal());
        System.out.println("[VendingMachine] Available payment methods: COIN, UPI");
        System.out.println("[VendingMachine] Select more products or call processPayment() to checkout.");
        return true;
    }

    /**
     * Called by ProductSelectionState or PaymentPendingState to process payment.
     * Creates a PaymentRequest with cart total as amountDue.
     * Transitions to PAYMENT_PENDING state for actual processing.
     */
    public PaymentResult processPaymentInState(String method, double amountPaid) {
        this.paymentMethod = method.toUpperCase();
        this.amountPaid = amountPaid;
        this.payerId = "USER-" + System.currentTimeMillis() % 10000;

        if (cart.isEmpty()) {
            return new PaymentResult(false, "Cart is empty. Select products first.", 0, Collections.emptyList());
        }

        double cartTotal = getCartTotal();

        PaymentStrategy strategy = strategyFactory.getStrategy(method);
        if (strategy == null) {
            return new PaymentResult(false, "Invalid payment method: " + method
                    + ". Available: COIN, UPI", amountPaid, Collections.emptyList());
        }

        // Transition to PAYMENT_PENDING state
        setState(new PaymentPendingState(this));
        System.out.println("[VendingMachine] State → PAYMENT_PENDING");

        // For COIN payments, deposit the inserted coins into the bank first
        if (method.equalsIgnoreCase("COIN") && amountPaid > 0) {
            List<Coin> insertedCoins = convertAmountToCoins(amountPaid);
            coinBank.depositCoins(insertedCoins);
        }

        // Create the appropriate payment request subclass based on payment method
        // cartTotal = total price of all products in the cart (the amount the user needs to pay)
        PaymentRequest paymentRequest;
        if (method.equalsIgnoreCase("COIN")) {
            paymentRequest = new CoinPayment(amountPaid, cartTotal, payerId);
        } else if (method.equalsIgnoreCase("UPI")) {
            paymentRequest = new UpiPayment(amountPaid, cartTotal, payerId,
                    payerId, "QR-" + System.currentTimeMillis());
        } else {
            paymentRequest = new CoinPayment(amountPaid, cartTotal, payerId);
        }
        PaymentResult strategyResult = strategy.pay(paymentRequest);

        if (!strategyResult.isSuccess()) {
            // Insufficient payment → refund all coins
            System.out.println("[VendingMachine] Payment failed: " + strategyResult.getMessage());
            if (method.equalsIgnoreCase("COIN") && amountPaid > 0) {
                coinBank.refundCoins(amountPaid);
                System.out.println("[VendingMachine] Refunded ₹" + amountPaid + " in coins.");
            }
            refundAndCancel();
            setState(new IdleState(this));
            return strategyResult;
        }

        // Payment successful — handle change (only for COIN payments)
        double changeAmount = strategyResult.getChangeAmount();
        List<Coin> changeCoins = Collections.emptyList();

        if (changeAmount > 0 && method.equalsIgnoreCase("COIN")) {
            changeCoins = coinBank.withdrawChange(changeAmount);
            if (changeCoins == null) {
                // Cannot make exact change → full refund
                System.out.println("[VendingMachine] Cannot make change for ₹" + changeAmount
                        + ". Full refund of ₹" + amountPaid + ".");
                coinBank.refundCoins(amountPaid);
                refundAndCancel();
                setState(new IdleState(this));
                return new PaymentResult(false,
                        "Cannot make change. Transaction cancelled. Full refund of ₹" + amountPaid,
                        amountPaid, Collections.emptyList());
            }
        }

        // Transition to dispensing state
        setState(new DispensingState(this));
        System.out.println("[VendingMachine] State → DISPENSING");

        // Dispense all products in cart
        boolean allDispensed = dispenseAllProducts();

        if (!allDispensed) {
            // Refund everything
            if (method.equalsIgnoreCase("COIN")) {
                coinBank.refundCoins(amountPaid);
            }
            refundAndCancel();
            setState(new IdleState(this));
            return new PaymentResult(false, "Error dispensing products. Full refund processed.",
                    amountPaid, Collections.emptyList());
        }

        // For UPI payments, credit the amount to the digital account
        if (method.equalsIgnoreCase("UPI")) {
            digitalAccount.credit(amountPaid);
        }

        // Show change info (only COIN payments have change)
        if (changeAmount <= 0) {
            System.out.println("[VendingMachine] No change to return.");
        } else {
            System.out.println("[VendingMachine] Change returned: ₹" + changeAmount
                    + " (" + changeCoins.size() + " coins): " + changeCoins);
        }

        // Release machine and go back to idle
        setState(new IdleState(this));
        accessLock.release();

        String successMsg = "✅ " + cart.size() + " product(s) dispensed!"
                + " Method: " + method.toUpperCase()
                + " | Paid: ₹" + amountPaid
                + " | Change: ₹" + changeAmount;

        return new PaymentResult(true, successMsg, changeAmount, changeCoins);
    }

    /**
     * Called by DispensingState to dispense all products in the cart.
     */
    public boolean dispenseProductInState() {
        return dispenseAllProducts();
    }

    private boolean dispenseAllProducts() {
        if (cart.isEmpty()) {
            return false;
        }
        for (Product product : cart) {
            Product dispensed = inventory.dispense(product.getId());
            if (dispensed == null) {
                System.out.println("[VendingMachine] Failed to dispense: " + product.getName());
                return false;
            }
            System.out.println("[VendingMachine] Dispensing: " + dispensed.getName());
        }
        return true;
    }

    /**
     * Called when a transaction needs to be refunded and cleaned up.
     */
    public void refundAndCancel() {
        System.out.println("[VendingMachine] Refunding transaction...");
        resetTransaction();
        if (accessLock.availablePermits() == 0) {
            accessLock.release();
        }
    }

    // ========== HELPERS ==========

    private double getCartTotal() {
        return cart.stream().mapToDouble(Product::getPrice).sum();
    }

    private void resetTransaction() {
        cart.clear();
        this.amountPaid = 0;
        this.paymentMethod = null;
        this.payerId = null;
    }

    /**
     * Converts a monetary amount into a list of Coin objects (greedy algorithm).
     */
    public List<Coin> convertAmountToCoins(double amount) {
        int amountInRupees = (int) Math.round(amount);
        List<Coin> result = new ArrayList<>();
        List<Coin> sortedCoins = Arrays.asList(Coin.TEN, Coin.FIVE, Coin.TWO, Coin.ONE);
        int remaining = amountInRupees;
        for (Coin coin : sortedCoins) {
            int value = coin.getValue();
            int count = remaining / value;
            for (int i = 0; i < count; i++) {
                result.add(coin);
            }
            remaining -= count * value;
        }
        return result;
    }

    public void printProductInventory() {
        inventory.printInventory();
    }

    public void printCoinInventory() {
        coinBank.printInventory();
    }

    public void printDigitalAccount() {
        digitalAccount.printStatement();
    }
}