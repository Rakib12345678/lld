package com.rocky.vendingmachine.payment;

import java.util.Collections;

/**
 * Payment strategy for UPI (digital) payments.
 * User scans a QR code displayed on the machine and pays the exact cart total.
 * The machine generates a QR with the exact amount — user just scans and confirms.
 * No overpayment or underpayment is possible since the amount is fixed in the QR.
 *
 * amountPaid = exact amount paid via UPI (must equal cartTotalPrice)
 * cartTotalPrice = total price of all products in the cart
 */
public class UPIPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(PaymentRequest request) {
        double cartTotalPrice = request.getCartTotalPrice();
        double amountPaid = request.getAmountPaid();

        if (amountPaid != cartTotalPrice) {
            return new PaymentResult(false,
                    "UPI payment mismatch. Expected ₹" + cartTotalPrice + " but got ₹" + amountPaid,
                    amountPaid,
                    Collections.emptyList());
        }

        String message = "Payment successful via UPI. Payer: " + request.getPayerId();

        return new PaymentResult(true, message, 0, Collections.emptyList());
    }
}