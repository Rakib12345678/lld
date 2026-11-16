package com.rocky.vendingmachine.state;

import com.rocky.vendingmachine.core.VendingMachine;
import com.rocky.vendingmachine.payment.PaymentResult;

/**
 * Abstract base class for Vending Machine State Design Pattern.
 * Holds the VendingMachine reference so implementations don't repeat it.
 * Each state defines allowed operations and transitions.
 */
public abstract class VendingMachineState {

    protected final VendingMachine machine;

    public VendingMachineState(VendingMachine machine) {
        this.machine = machine;
    }

    /**
     * Handles product selection in the current state.
     * @param productId the product ID to select
     * @return true if product was selected successfully
     */
    public abstract boolean selectProduct(String productId);

    /**
     * Handles payment processing in the current state.
     * @param method payment method (COIN/UPI)
     * @param amountPaid amount paid
     * @return PaymentResult
     */
    public abstract PaymentResult processPayment(String method, double amountPaid);

    /**
     * Handles cancel/refund in the current state.
     */
    public abstract void cancelTransaction();

    /**
     * Dispenses the product in the current state.
     * @return true if product was dispensed
     */
    public abstract boolean dispenseProduct();

    /**
     * Returns the name of the current state.
     */
    public abstract String getStateName();
}