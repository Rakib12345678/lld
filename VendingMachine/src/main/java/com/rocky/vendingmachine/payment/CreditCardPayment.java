package com.rocky.vendingmachine.payment;

/**
 * Payment request for CREDIT_CARD payments.
 * Contains card-specific details: card number, cardholder name, expiry date, and CVV.
 */
public class CreditCardPayment extends PaymentRequest {

    private final String cardNumber;
    private final String cardHolderName;
    private final String expiryDate;
    private final String cvv;

    public CreditCardPayment(double amountPaid, double cartTotalPrice, String payerId,
                             String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        super(amountPaid, cartTotalPrice, payerId);
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    @Override
    public String toString() {
        return super.toString() + " CreditCardPayment{" +
                "cardHolder='" + cardHolderName + '\'' +
                ", expiry='" + expiryDate + '\'' +
                '}';
    }
}