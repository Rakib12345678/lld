package com.rocky.vendingmachine.state;

import com.rocky.vendingmachine.core.VendingMachine;
import com.rocky.vendingmachine.payment.PaymentResult;

import java.util.Collections;

/**
 * Idle state — machine is waiting for a user.
 * ONLY selectProduct() is allowed (triggers machine acquisition).
 * All other operations are blocked.
 */
public class IdleState extends VendingMachineState {

    public IdleState(VendingMachine machine) {
        super(machine);
    }

    @Override
    public boolean selectProduct(String productId) {
        System.out.println("[IdleState] Machine is idle. Please acquire the machine first.");
        return false;
    }

    @Override
    public PaymentResult processPayment(String method, double amountPaid) {
        System.out.println("[IdleState] Machine is idle. Please acquire the machine first.");
        return new PaymentResult(false, "Machine is idle. Acquire machine first.", 0, Collections.emptyList());
    }

    @Override
    public void cancelTransaction() {
        System.out.println("[IdleState] No active transaction to cancel.");
    }

    @Override
    public boolean dispenseProduct() {
        System.out.println("[IdleState] No product to dispense. Machine is idle.");
        return false;
    }

    @Override
    public String getStateName() {
        return "IDLE";
    }
}