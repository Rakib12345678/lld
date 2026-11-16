package com.rocky.vendingmachine.payment;

import java.util.Collections;

/**
 * Payment strategy using physical coins with change/exchange capability.
 * Receives a generic PaymentRequest — validates amount and calculates change.
 * CoinBank operations (deposit/withdraw) are handled by VendingMachine.
 *
 * amountPaid = what the user inserted (can be more than cart total)
 * cartTotalPrice = total price of all products in the cart
 * change = amountPaid - cartTotalPrice (if positive)
 */
public class CoinPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(PaymentRequest request) {
        double cartTotalPrice = request.getCartTotalPrice();
        double amountPaid = request.getAmountPaid();

        if (amountPaid < cartTotalPrice) {
            return new PaymentResult(false,
                    "Insufficient payment. Need ₹" + cartTotalPrice + " but got ₹" + amountPaid,
                    amountPaid,
                    Collections.emptyList());
        }

        double changeAmount = amountPaid - cartTotalPrice;

        String message = "Payment successful via COINS. Payer: " + request.getPayerId();

        return new PaymentResult(true, message, changeAmount, Collections.emptyList());
    }
}