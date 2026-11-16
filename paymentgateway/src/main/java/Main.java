import dto.PaymentRequest;
import dto.PaymentResponse;
import enums.PaymentMethod;
import service.PaymentGatewayservice;
import service.TransactionService;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        TransactionService txnService = new TransactionService();
        PaymentGatewayservice gateway = new PaymentGatewayservice(txnService);

        System.out.println("========================================");
        System.out.println("   PAYMENT GATEWAY SYSTEM DEMO");
        System.out.println("========================================\n");

        // Test 1: PayPal Payment
        System.out.println("--- Test 1: PayPal Payment ---");
        PaymentRequest paypalRequest = new PaymentRequest(
                "idem-001",
                PaymentMethod.PAYPAL,
                150.00,
                "user123",
                new HashMap<>()
        );
        PaymentResponse paypalResponse = gateway.processPayment(paypalRequest);
        System.out.println("Response: " + paypalResponse + "\n");

        // Test 2: UPI Payment
        System.out.println("--- Test 2: UPI Payment ---");
        PaymentRequest upiRequest = new PaymentRequest(
                "idem-002",
                PaymentMethod.UPI,
                250.00,
                "user456",
                new HashMap<>()
        );
        PaymentResponse upiResponse = gateway.processPayment(upiRequest);
        System.out.println("Response: " + upiResponse + "\n");

        // Test 3: NetBanking Payment
        System.out.println("--- Test 3: NetBanking Payment ---");
        PaymentRequest netBankingRequest = new PaymentRequest(
                "idem-003",
                PaymentMethod.NET_BANKING,
                500.00,
                "user789",
                new HashMap<>()
        );
        PaymentResponse netBankingResponse = gateway.processPayment(netBankingRequest);
        System.out.println("Response: " + netBankingResponse + "\n");

        // Test 4: Idempotency (same request again - should return existing transaction)
        System.out.println("--- Test 4: Idempotency Check ---");
        PaymentResponse duplicateResponse = gateway.processPayment(paypalRequest);
        System.out.println("Duplicate Response: " + duplicateResponse + "\n");

        // Test 5: Retrieve transaction by ID
        System.out.println("--- Test 5: Transaction Lookup ---");
        if (upiResponse.getTransactionId() != null) {
            var transaction = gateway.getTransaction(upiResponse.getTransactionId());
            System.out.println("Retrieved Transaction: " +
                    "ID=" + transaction.getId() +
                    " | Amount=$" + transaction.getAmount() +
                    " | Status=" + transaction.getPaymentStatus() +
                    " | Method=" + transaction.getPaymentMethod());
        }
        System.out.println();

        // Print all transactions summary
        gateway.printAllTransactions();

        System.out.println("\nTotal Transactions Processed: " + gateway.getTotalTransactionCount());
        System.out.println("========================================");
    }
}