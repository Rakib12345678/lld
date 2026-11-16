package dto;

import enums.PaymentMethod;

import java.util.HashMap;

public class PaymentRequest {
    String idempotencyKey;
    PaymentMethod paymentMethod;
    double amount;
    String userId;
    HashMap<String, String> metadata;

    public PaymentRequest(String idempotencyKey, PaymentMethod paymentMethod, double amount, String userId, HashMap<String, String> metadata) {
        this.idempotencyKey = idempotencyKey;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.userId = userId;
        this.metadata = metadata;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public String getUserId() {
        return userId;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }
}
