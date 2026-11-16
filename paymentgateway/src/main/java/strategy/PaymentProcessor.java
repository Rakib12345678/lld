package strategy;

import dto.PaymentRequest;
import dto.PaymentResponse;

public interface PaymentProcessor {
    PaymentResponse processPayment(PaymentRequest request);
}