package dto;

import enums.PaymentStatus;

public class PaymentResponse {
    private String transactionId;
    private PaymentStatus status;
    private String message;
    private double amount;
    private String userId;

    public PaymentResponse(String transactionId, PaymentStatus status, String message, double amount, String userId) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.amount = amount;
        this.userId = userId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public double getAmount() {
        return amount;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", amount=" + amount +
                ", userId='" + userId + '\'' +
                '}';
    }
}