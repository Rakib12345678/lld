package com.rocky.vendingmachine.state;

import com.rocky.vendingmachine.core.VendingMachine;
import com.rocky.vendingmachine.payment.PaymentResult;

import java.util.Collections;

/**
 * Payment Pending state — payment is being processed.
 * ONLY processPayment() is allowed. All other operations are blocked.
 * On success: transitions to DispensingState.
 * On failure: transitions to IdleState (with refund).
 */
public class PaymentPendingState extends VendingMachineState {

    public PaymentPendingState(VendingMachine machine) {
        super(machine);
    }

    @Override
    public boolean selectProduct(String productId) {
        System.out.println("[PaymentPendingState] Cannot select product. Payment in progress.");
        return false;
    }

    @Override
    public PaymentResult processPayment(String method, double amountPaid) {
        return machine.processPaymentInState(method, amountPaid);
    }

    @Override
    public void cancelTransaction() {
        System.out.println("[PaymentPendingState] Cancelling payment...");
        machine.refundAndCancel();
    }

    @Override
    public boolean dispenseProduct() {
        System.out.println("[PaymentPendingState] Cannot dispense. Payment not yet confirmed.");
        return false;
    }

    @Override
    public String getStateName() {
        return "PAYMENT_PENDING";
    }
}