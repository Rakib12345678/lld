package com.rocky.vendingmachine.payment;

/**
 * Payment request for COIN payments.
 * No extra fields beyond the base PaymentRequest — coins are identified by the payment type itself.
 */
public class CoinPayment extends PaymentRequest {

    public CoinPayment(double amountPaid, double cartTotalPrice, String payerId) {
        super(amountPaid, cartTotalPrice, payerId);
    }
}