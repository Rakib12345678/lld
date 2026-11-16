package strategy;

import dto.PaymentRequest;
import dto.PaymentResponse;
import enums.PaymentStatus;

import java.util.UUID;

public class UpiStrategy implements PaymentProcessor {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        System.out.println("Processing UPI payment of $" + request.getAmount() + " for user: " + request.getUserId());

        boolean paymentSuccess = simulatePaymentGatewayCall();

        if (paymentSuccess) {
            String transactionId = UUID.randomUUID().toString();
            System.out.println("UPI payment successful. Transaction ID: " + transactionId);
            return new PaymentResponse(
                    transactionId,
                    PaymentStatus.SUCCESS,
                    "UPI payment processed successfully",
                    request.getAmount(),
                    request.getUserId()
            );
        } else {
            System.out.println("UPI payment failed.");
            return new PaymentResponse(
                    null,
                    PaymentStatus.FAILURE,
                    "UPI payment processing failed",
                    request.getAmount(),
                    request.getUserId()
            );
        }
    }

    private boolean simulatePaymentGatewayCall() {
        // Simulate network call - 85% success rate
        return Math.random() > 0.15;
    }
}