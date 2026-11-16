package com.rocky.splitwise.service;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Settlement;
import com.rocky.splitwise.model.User;
import com.rocky.splitwise.observer.SettlementObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettlementService {
    private final List<Settlement> settlements = new CopyOnWriteArrayList<>();
    private final BalanceService balanceService;
    private final UserService userService;
    private final List<SettlementObserver> observers = new ArrayList<>();

    public SettlementService(BalanceService balanceService, UserService userService) {
        this.balanceService = balanceService;
        this.userService = userService;
    }

    public void addObserver(SettlementObserver observer) {
        observers.add(observer);
    }

    /**
     * Process a settlement: paidByUser pays amount to receivedByUser.
     * Supports partial settlements.
     *
     * @throws SplitWiseException if the payer doesn't owe enough (or at all) to the receiver
     */
    public Settlement settleUp(String settlementId, String paidByUserId, String receivedByUserId, double amount) {
        if (amount <= 0) {
            throw new SplitWiseException("Settlement amount must be positive");
        }

        User paidBy = userService.getUser(paidByUserId);
        User receivedBy = userService.getUser(receivedByUserId);

        if (paidBy == null) {
            throw new SplitWiseException("User not found: " + paidByUserId);
        }
        if (receivedBy == null) {
            throw new SplitWiseException("User not found: " + receivedByUserId);
        }

        // Check current balance: paidBy owes receivedBy (positive means paidBy owes receivedBy)
        double currentOwed = balanceService.getBalance(paidBy, receivedBy);
        if (currentOwed <= 0.01) {
            throw new SplitWiseException(paidBy.getName() + " does not owe anything to " + receivedBy.getName());
        }
        if (amount > currentOwed + 0.01) {
            throw new SplitWiseException("Settlement amount $" + String.format("%.2f", amount)
                    + " exceeds owed amount $" + String.format("%.2f", currentOwed)
                    + ". Partial settlement only up to what is owed.");
        }

        // Decrease the owed amount (paidBy owes less to receivedBy)
        balanceService.receiveOwing(paidBy, receivedBy, amount);

        Settlement settlement = new Settlement(settlementId, paidBy, receivedBy, amount);
        settlements.add(settlement);

        // Notify observers
        for (SettlementObserver observer : observers) {
            observer.onSettlementMade(settlement);
        }

        return settlement;
    }

    public List<Settlement> getAllSettlements() {
        return new ArrayList<>(settlements);
    }
}