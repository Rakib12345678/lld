package strategy;

import dto.PaymentRequest;
import dto.PaymentResponse;
import enums.PaymentStatus;

import java.util.UUID;

public class NetBankingStrategy implements PaymentProcessor {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        System.out.println("Processing NetBanking payment of $" + request.getAmount() + " for user: " + request.getUserId());

        boolean paymentSuccess = simulatePaymentGatewayCall();

        if (paymentSuccess) {
            String transactionId = UUID.randomUUID().toString();
            System.out.println("NetBanking payment successful. Transaction ID: " + transactionId);
            return new PaymentResponse(
                    transactionId,
                    PaymentStatus.SUCCESS,
                    "NetBanking payment processed successfully",
                    request.getAmount(),
                    request.getUserId()
            );
        } else {
            System.out.println("NetBanking payment failed.");
            return new PaymentResponse(
                    null,
                    PaymentStatus.FAILURE,
                    "NetBanking payment processing failed",
                    request.getAmount(),
                    request.getUserId()
            );
        }
    }

    private boolean simulatePaymentGatewayCall() {
        // Simulate network call - 80% success rate
        return Math.random() > 0.2;
    }
}