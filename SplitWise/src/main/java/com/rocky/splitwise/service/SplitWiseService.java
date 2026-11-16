package com.rocky.splitwise.service;

import com.rocky.splitwise.model.Expense;
import com.rocky.splitwise.model.Group;
import com.rocky.splitwise.model.Settlement;
import com.rocky.splitwise.model.SplitType;
import com.rocky.splitwise.model.User;
import com.rocky.splitwise.split.SplitStrategyFactory;

import java.util.List;

public class SplitWiseService {
    private final UserService userService;
    private final GroupService groupService;
    private final BalanceService balanceService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;
    private final NotificationService notificationService;
    private final SplitStrategyFactory splitStrategyFactory;

    public SplitWiseService() {
        this.userService = new UserService();
        this.balanceService = new BalanceService();
        this.splitStrategyFactory = new SplitStrategyFactory();
        this.groupService = new GroupService(userService);
        this.expenseService = new ExpenseService(balanceService, userService, groupService, splitStrategyFactory);
        this.settlementService = new SettlementService(balanceService, userService);
        this.notificationService = new NotificationService();

        // Register observers
        expenseService.addObserver(notificationService);
        settlementService.addObserver(notificationService);
    }

    // User operations
    public User addUser(String id, String name, String email, String phone) {
        return userService.addUser(id, name, email, phone);
    }

    public User getUser(String id) {
        return userService.getUser(id);
    }

    // Group operations
    public Group createGroup(String id, String name) {
        return groupService.createGroup(id, name);
    }

    public void addMemberToGroup(String groupId, String userId) {
        groupService.addMemberToGroup(groupId, userId);
    }

    public Group getGroup(String groupId) {
        return groupService.getGroup(groupId);
    }

    // Expense operations - direct participants
    public Expense addExpense(String id, String description, double amount, String paidByUserId,
                               List<String> participantUserIds, SplitType splitType,
                               List<Double> splitValues) {
        return expenseService.addExpense(id, description, amount, paidByUserId,
                participantUserIds, splitType, splitValues);
    }

    // Expense operations - group-based (automatically uses all group members as participants)
    public Expense addGroupExpense(String id, String description, double amount, String paidByUserId,
                                    String groupId, SplitType splitType,
                                    List<Double> splitValues) {
        return expenseService.addGroupExpense(id, description, amount, paidByUserId,
                groupId, splitType, splitValues);
    }

    // Settlement operations
    public Settlement settleUp(String settlementId, String paidByUserId, String receivedByUserId, double amount) {
        return settlementService.settleUp(settlementId, paidByUserId, receivedByUserId, amount);
    }

    // Balance operations
    public double getBalance(String userId1, String userId2) {
        User user1 = userService.getUser(userId1);
        User user2 = userService.getUser(userId2);
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("User not found");
        }
        return balanceService.getBalance(user1, user2);
    }

    public void printBalances() {
        balanceService.printBalances(userService);
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}