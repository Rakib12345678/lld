package strategy;

import dto.PaymentRequest;
import dto.PaymentResponse;
import enums.PaymentStatus;

import java.util.UUID;

public class PaypalStrategy implements PaymentProcessor {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulate PayPal payment processing
        System.out.println("Processing PayPal payment of $" + request.getAmount() + " for user: " + request.getUserId());

        // Simulate payment success/failure logic
        boolean paymentSuccess = simulatePaymentGatewayCall();

        if (paymentSuccess) {
            String transactionId = UUID.randomUUID().toString();
            System.out.println("PayPal payment successful. Transaction ID: " + transactionId);
            return new PaymentResponse(
                    transactionId,
                    PaymentStatus.SUCCESS,
                    "PayPal payment processed successfully",
                    request.getAmount(),
                    request.getUserId()
            );
        } else {
            System.out.println("PayPal payment failed.");
            return new PaymentResponse(
                    null,
                    PaymentStatus.FAILURE,
                    "PayPal payment processing failed",
                    request.getAmount(),
                    request.getUserId()
            );
        }
    }

    private boolean simulatePaymentGatewayCall() {
        // Simulate network call - 90% success rate
        return Math.random() > 0.1;
    }
}