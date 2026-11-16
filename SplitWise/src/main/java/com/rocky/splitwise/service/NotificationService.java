package com.rocky.splitwise.service;

import com.rocky.splitwise.model.Expense;
import com.rocky.splitwise.model.Settlement;
import com.rocky.splitwise.model.User;
import com.rocky.splitwise.observer.ExpenseObserver;
import com.rocky.splitwise.observer.SettlementObserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements ExpenseObserver, SettlementObserver {

    private final List<Expense> expenseNotifications = new ArrayList<>();
    private final List<Settlement> settlementNotifications = new ArrayList<>();

    @Override
    public void onExpenseAdded(Expense expense) {
        expenseNotifications.add(expense);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[NOTIFICATION " + timestamp + "] Expense added: " + expense.getDescription()
                + " - $" + expense.getAmount() + " paid by " + expense.getPaidBy().getName()
                + " | Split type: " + expense.getSplitType());

        for (User participant : expense.getParticipants()) {
            System.out.println("  -> Notified " + participant.getName()
                    + " (" + participant.getEmail() + ") about expense: " + expense.getDescription());
        }
    }

    @Override
    public void onSettlementMade(Settlement settlement) {
        settlementNotifications.add(settlement);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[NOTIFICATION " + timestamp + "] Settlement: " + settlement.getPaidBy().getName()
                + " paid $" + settlement.getAmount() + " to " + settlement.getReceivedBy().getName());

        System.out.println("  -> Notified " + settlement.getPaidBy().getName()
                + " (" + settlement.getPaidBy().getEmail() + "): Payment sent");
        System.out.println("  -> Notified " + settlement.getReceivedBy().getName()
                + " (" + settlement.getReceivedBy().getEmail() + "): Payment received");
    }

    public List<Expense> getExpenseNotifications() {
        return new ArrayList<>(expenseNotifications);
    }

    public List<Settlement> getSettlementNotifications() {
        return new ArrayList<>(settlementNotifications);
    }
}