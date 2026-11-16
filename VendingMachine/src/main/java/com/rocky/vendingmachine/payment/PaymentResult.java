package com.rocky.vendingmachine.payment;

import com.rocky.vendingmachine.model.Coin;

import java.util.List;

public class PaymentResult {
    private final boolean success;
    private final String message;
    private final double changeAmount;
    private final List<Coin> changeCoins;

    public PaymentResult(boolean success, String message, double changeAmount, List<Coin> changeCoins) {
        this.success = success;
        this.message = message;
        this.changeAmount = changeAmount;
        this.changeCoins = changeCoins;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public List<Coin> getChangeCoins() {
        return changeCoins;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", changeAmount=" + changeAmount +
                '}';
    }
}