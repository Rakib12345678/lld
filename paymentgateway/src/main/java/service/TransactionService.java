package service;

import model.Transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionService {
    private final Map<String, Transaction> transactionStore = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyStore = new ConcurrentHashMap<>();

    public void saveTransaction(Transaction transaction) {
        transactionStore.put(transaction.getId(), transaction);
        idempotencyStore.put(transaction.getIdempotencyKey(), transaction.getId());
    }

    public Transaction getTransactionById(String transactionId) {
        return transactionStore.get(transactionId);
    }

    public boolean isIdempotencyKeyProcessed(String idempotencyKey) {
        return idempotencyStore.containsKey(idempotencyKey);
    }

    public String getTransactionIdByIdempotencyKey(String idempotencyKey) {
        return idempotencyStore.get(idempotencyKey);
    }

    public void updateTransactionStatus(String transactionId, String status) {
        Transaction transaction = transactionStore.get(transactionId);
        if (transaction != null) {
            transaction.setPaymentStatus(status);
        }
    }

    public int getTotalTransactionCount() {
        return transactionStore.size();
    }

    public void printAllTransactions() {
        System.out.println("\n=== All Transactions ===");
        for (Transaction txn : transactionStore.values()) {
            System.out.println("ID: " + txn.getId() +
                    " | IdempotencyKey: " + txn.getIdempotencyKey() +
                    " | Amount: $" + txn.getAmount() +
                    " | User: " + txn.getUserId() +
                    " | Method: " + txn.getPaymentMethod() +
                    " | Status: " + txn.getPaymentStatus());
        }
        System.out.println("========================");
    }
}