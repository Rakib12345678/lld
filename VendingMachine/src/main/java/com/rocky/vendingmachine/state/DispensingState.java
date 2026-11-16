package com.rocky.vendingmachine.state;

import com.rocky.vendingmachine.core.VendingMachine;
import com.rocky.vendingmachine.payment.PaymentResult;

import java.util.Collections;

/**
 * Dispensing state — payment successful, product is being dispensed.
 * ONLY dispenseProduct() is allowed. All other operations are blocked.
 * On successful dispense: transitions to IdleState.
 */
public class DispensingState extends VendingMachineState {

    public DispensingState(VendingMachine machine) {
        super(machine);
    }

    @Override
    public boolean selectProduct(String productId) {
        System.out.println("[DispensingState] Cannot select product. Dispensing in progress.");
        return false;
    }

    @Override
    public PaymentResult processPayment(String method, double amountPaid) {
        System.out.println("[DispensingState] Cannot process payment. Dispensing in progress.");
        return new PaymentResult(false, "Dispensing in progress. Please wait.", 0, Collections.emptyList());
    }

    @Override
    public void cancelTransaction() {
        System.out.println("[DispensingState] Cannot cancel. Product is being dispensed.");
    }

    @Override
    public boolean dispenseProduct() {
        boolean success = machine.dispenseProductInState();
        if (success) {
            System.out.println("[DispensingState] Product dispensed successfully.");
            machine.setState(new IdleState(machine));
        } else {
            System.out.println("[DispensingState] Product out of stock! Refunding...");
            machine.refundAndCancel();
            machine.setState(new IdleState(machine));
        }
        return success;
    }

    @Override
    public String getStateName() {
        return "DISPENSING";
    }
}