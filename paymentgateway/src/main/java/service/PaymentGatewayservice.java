package service;

import dto.PaymentRequest;
import dto.PaymentResponse;
import enums.PaymentMethod;
import enums.PaymentStatus;
import model.Transaction;
import strategy.PaymentFactory;
import strategy.PaymentProcessor;

import java.util.UUID;

public class PaymentGatewayservice {

    private final TransactionService transactionService;

    public PaymentGatewayservice(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Processes a payment request with idempotency support.
     * If the same idempotencyKey was already processed, returns the existing transaction result.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Idempotency Check
        if (transactionService.isIdempotencyKeyProcessed(request.getIdempotencyKey())) {
            String existingTransactionId = transactionService.getTransactionIdByIdempotencyKey(request.getIdempotencyKey());
            Transaction existingTxn = transactionService.getTransactionById(existingTransactionId);
            System.out.println("Idempotency hit! Returning existing transaction: " + existingTransactionId);
            return new PaymentResponse(
                    existingTxn.getId(),
                    PaymentStatus.valueOf(existingTxn.getPaymentStatus()),
                    "Duplicate request - returning existing transaction",
                    existingTxn.getAmount(),
                    existingTxn.getUserId()
            );
        }

        // 2. Get the appropriate payment processor via Factory
        PaymentProcessor processor = PaymentFactory.getPaymentProcessor(request.getPaymentMethod());

        // 3. Process payment
        PaymentResponse response = processor.processPayment(request);

        // 4. Create and persist transaction
        String transactionId = response.getTransactionId() != null ? response.getTransactionId() : UUID.randomUUID().toString();
        Transaction transaction = new Transaction(
                transactionId,
                request.getIdempotencyKey(),
                request.getAmount(),
                request.getUserId(),
                request.getPaymentMethod().name(),
                response.getStatus().name()
        );
        transactionService.saveTransaction(transaction);

        return response;
    }

    /**
     * Retrieves a transaction by its ID.
     */
    public Transaction getTransaction(String transactionId) {
        return transactionService.getTransactionById(transactionId);
    }

    /**
     * Prints all transactions.
     */
    public void printAllTransactions() {
        transactionService.printAllTransactions();
    }

    /**
     * Gets total transaction count.
     */
    public int getTotalTransactionCount() {
        return transactionService.getTotalTransactionCount();
    }

    /**
     * Simple demo to test all payment methods.
     */
    public static void main(String[] args) {
        TransactionService txnService = new TransactionService();
        PaymentGatewayservice gateway = new PaymentGatewayservice(txnService);

        System.out.println("========================================");
        System.out.println("   PAYMENT GATEWAY SYSTEM DEMO");
        System.out.println("========================================\n");

        // Test PayPal payment
        PaymentRequest paypalRequest = new PaymentRequest(
                "idem-001",
                PaymentMethod.PAYPAL,
                150.00,
                "user123",
                new java.util.HashMap<>()
        );
        PaymentResponse paypalResponse = gateway.processPayment(paypalRequest);
        System.out.println("Response: " + paypalResponse + "\n");

        // Test UPI payment
        PaymentRequest upiRequest = new PaymentRequest(
                "idem-002",
                PaymentMethod.UPI,
                250.00,
                "user456",
                new java.util.HashMap<>()
        );
        PaymentResponse upiResponse = gateway.processPayment(upiRequest);
        System.out.println("Response: " + upiResponse + "\n");

        // Test NetBanking payment
        PaymentRequest netBankingRequest = new PaymentRequest(
                "idem-003",
                PaymentMethod.NET_BANKING,
                500.00,
                "user789",
                new java.util.HashMap<>()
        );
        PaymentResponse netBankingResponse = gateway.processPayment(netBankingRequest);
        System.out.println("Response: " + netBankingResponse + "\n");

        // Test Idempotency - same request again
        System.out.println("--- Testing Idempotency (sending same request again) ---");
        PaymentResponse duplicateResponse = gateway.processPayment(paypalRequest);
        System.out.println("Duplicate Response: " + duplicateResponse + "\n");

        // Print all transactions
        gateway.printAllTransactions();

        System.out.println("\nTotal Transactions: " + gateway.getTotalTransactionCount());
        System.out.println("========================================");
    }
}