package com.rocky.vendingmachine.state;

import com.rocky.vendingmachine.core.VendingMachine;
import com.rocky.vendingmachine.payment.PaymentResult;

import java.util.Collections;

/**
 * Product Selection state — user is selecting products.
 * selectProduct() is allowed (add to cart).
 * processPayment() transitions to PAYMENT_PENDING.
 * cancelTransaction() returns to IDLE.
 */
public class ProductSelectionState extends VendingMachineState {

    public ProductSelectionState(VendingMachine machine) {
        super(machine);
    }

    @Override
    public boolean selectProduct(String productId) {
        return machine.selectProductInState(productId);
    }

    @Override
    public PaymentResult processPayment(String method, double amountPaid) {
        return machine.processPaymentInState(method, amountPaid);
    }

    @Override
    public void cancelTransaction() {
        machine.refundAndCancel();
    }

    @Override
    public boolean dispenseProduct() {
        System.out.println("[ProductSelectionState] Cannot dispense. Complete payment first.");
        return false;
    }

    @Override
    public String getStateName() {
        return "PRODUCT_SELECTION";
    }
}