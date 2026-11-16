package com.rocky.vendingmachine.payment;

/**
 * Strategy interface for different payment methods.
 * Receives a polymorphic PaymentRequest — subclasses carry payment-specific data.
 * The strategy only validates and processes the payment itself using base class getters.
 */
public interface PaymentStrategy {
    /**
     * Process payment based on the payment request.
     * @param request a PaymentRequest subclass (CoinPayment, UpiPayment, CreditCardPayment, etc.)
     *                containing amount paid, amount due, payer info, and payment-specific details.
     * @return a PaymentResult with success/failure, change, and receipt info
     */
    PaymentResult pay(PaymentRequest request);
}
