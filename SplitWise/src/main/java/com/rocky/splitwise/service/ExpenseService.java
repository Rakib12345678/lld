package com.rocky.splitwise.service;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Expense;
import com.rocky.splitwise.model.Group;
import com.rocky.splitwise.model.SplitType;
import com.rocky.splitwise.model.User;
import com.rocky.splitwise.observer.ExpenseObserver;
import com.rocky.splitwise.split.SplitStrategy;
import com.rocky.splitwise.split.SplitStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExpenseService {
    private final List<Expense> expenses = new CopyOnWriteArrayList<>();
    private final BalanceService balanceService;
    private final UserService userService;
    private final GroupService groupService;
    private final SplitStrategyFactory splitStrategyFactory;
    private final List<ExpenseObserver> observers = new ArrayList<>();

    public ExpenseService(BalanceService balanceService, UserService userService,
                          GroupService groupService, SplitStrategyFactory splitStrategyFactory) {
        this.balanceService = balanceService;
        this.userService = userService;
        this.groupService = groupService;
        this.splitStrategyFactory = splitStrategyFactory;
    }

    public void addObserver(ExpenseObserver observer) {
        observers.add(observer);
    }

    /**
     * Add an expense between a list of direct participants (one-to-one or group of users).
     */
    public Expense addExpense(String id, String description, double amount, String paidByUserId,
                              List<String> participantUserIds, SplitType splitType,
                              List<Double> splitValues) {
        return addExpenseInternal(id, description, amount, paidByUserId,
                participantUserIds, splitType, splitValues, null);
    }

    /**
     * Add an expense for an entire group. Participants are automatically resolved from the group members.
     */
    public Expense addGroupExpense(String id, String description, double amount, String paidByUserId,
                                   String groupId, SplitType splitType,
                                   List<Double> splitValues) {
        Group group = groupService.getGroup(groupId);
        if (group == null) {
            throw new SplitWiseException("Group not found: " + groupId);
        }

        List<String> participantUserIds = new ArrayList<>();
        for (User member : group.getMembers()) {
            participantUserIds.add(member.getId());
        }

        return addExpenseInternal(id, description, amount, paidByUserId,
                participantUserIds, splitType, splitValues, groupId);
    }

    private Expense addExpenseInternal(String id, String description, double amount, String paidByUserId,
                                       List<String> participantUserIds, SplitType splitType,
                                       List<Double> splitValues, String groupId) {
        User paidBy = userService.getUser(paidByUserId);
        if (paidBy == null) {
            throw new SplitWiseException("User not found: " + paidByUserId);
        }

        List<User> participants = new ArrayList<>();
        for (String userId : participantUserIds) {
            User user = userService.getUser(userId);
            if (user == null) {
                throw new SplitWiseException("User not found: " + userId);
            }
            participants.add(user);
        }

        Expense expense = new Expense(id, description, amount, paidBy, participants,
                splitType, splitValues, groupId);

        SplitStrategy strategy = splitStrategyFactory.getStrategy(splitType);
        strategy.validate(expense);

        // Returns Map<userId, amount> — each participant mapped directly to their share
        Map<String, Double> splitAmounts = strategy.calculateSplitAmounts(expense);

        // Update balances: each participant owes the paidBy user their share
        for (User participant : participants) {
            String participantId = participant.getId();

            // Skip the person who paid (they don't owe themselves)
            if (participantId.equals(paidBy.getId())) {
                continue;
            }

            double share = splitAmounts.get(participantId);
            balanceService.addOwing(participant, paidBy, share);
        }

        expenses.add(expense);

        // Notify observers
        for (ExpenseObserver observer : observers) {
            observer.onExpenseAdded(expense);
        }

        return expense;
    }

    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }
}