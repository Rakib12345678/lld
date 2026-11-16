package com.rocky.vendingmachine.payment;

/**
 * Abstract base class for all payment requests.
 * Contains shared fields: amount paid, cart total price, and payer ID.
 * Subclasses add payment-specific fields (e.g., card details, UPI ID).
 */
public abstract class PaymentRequest {
    private final double amountPaid;
    private final double cartTotalPrice;
    private final String payerId;

    protected PaymentRequest(double amountPaid, double cartTotalPrice, String payerId) {
        this.amountPaid = amountPaid;
        this.cartTotalPrice = cartTotalPrice;
        this.payerId = payerId;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    /**
     * @return The total price of all products in the cart (the amount the user needs to pay)
     */
    public double getCartTotalPrice() {
        return cartTotalPrice;
    }

    public String getPayerId() {
        return payerId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "paid=" + amountPaid +
                ", cartTotal=" + cartTotalPrice +
                ", payer='" + payerId + '\'' +
                '}';
    }
}