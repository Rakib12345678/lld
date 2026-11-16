package com.rocky.splitwise;

import com.rocky.splitwise.model.SplitType;
import com.rocky.splitwise.service.SplitWiseService;

import java.util.Arrays;

/**
 * Demo to test all requirements of SplitWise LLD:
 * - User registration
 * - Group creation
 * - One-to-one expense (direct participants)
 * - Group-based expense (participants resolved from group)
 * - Equal split with rounding
 * - Exact split
 * - Percentage split
 * - Balance tracking (pairwise net balances)
 * - Partial settlement
 * - Full settlement
 * - Notifications on expenses and settlements
 */
public class SplitWiseDemo {

    public static void main(String[] args) {
        SplitWiseService splitWise = new SplitWiseService();

        System.out.println("========================================");
        System.out.println("  SPLITWISE - LOW LEVEL DESIGN DEMO");
        System.out.println("========================================");

        // ===== 1. Create Users =====
        System.out.println("\n--- STEP 1: Adding Users ---");
        var alice = splitWise.addUser("u1", "Alice", "alice@example.com", "111-111-1111");
        var bob = splitWise.addUser("u2", "Bob", "bob@example.com", "222-222-2222");
        var charlie = splitWise.addUser("u3", "Charlie", "charlie@example.com", "333-333-3333");
        var diana = splitWise.addUser("u4", "Diana", "diana@example.com", "444-444-4444");
        System.out.println("Users created: " + alice.getName() + ", " + bob.getName()
                + ", " + charlie.getName() + ", " + diana.getName());

        // ===== 2. Create Group =====
        System.out.println("\n--- STEP 2: Creating Group ---");
        var tripGroup = splitWise.createGroup("g1", "Weekend Trip");
        splitWise.addMemberToGroup("g1", "u1");
        splitWise.addMemberToGroup("g1", "u2");
        splitWise.addMemberToGroup("g1", "u3");
        splitWise.addMemberToGroup("g1", "u4");
        System.out.println("Group created: " + tripGroup.getName() + " with "
                + tripGroup.getMembers().size() + " members");

        // ===================================================================
        // PATTERN A: Expense with direct participant list
        // Use addExpense() when you know exactly who the participants are
        // ===================================================================

        System.out.println("\n============= PATTERN A: DIRECT PARTICIPANTS =============");

        // ===== 3. Equal Split (direct participants) with Rounding =====
        System.out.println("\n--- Step 3A: Equal Split (Hotel $100 - Alice, Bob, Charlie) ---");
        // $100 split equally among 3 people: $33.33, $33.33, $33.34 (last absorbs rounding)
        splitWise.addExpense(
                "e1", "Hotel Booking", 100.0, "u1",         // Alice paid
                Arrays.asList("u1", "u2", "u3"),               // Alice, Bob, Charlie (direct list)
                SplitType.EQUAL, null                           // Equal split, no split values needed
        );
        splitWise.printBalances();

        // ===== 4. Exact Split (direct participants) =====
        System.out.println("--- Step 4A: Exact Split (Dinner $60 - Alice & Bob) ---");
        // $60 dinner: Alice pays $40, Bob pays $20 directly specified
        splitWise.addExpense(
                "e2", "Dinner at Restaurant", 60.0, "u2",     // Bob paid
                Arrays.asList("u1", "u2"),                       // Alice, Bob (direct list)
                SplitType.EXACT, Arrays.asList(40.0, 20.0)
        );
        splitWise.printBalances();

        // ===================================================================
        // PATTERN B: Group-based expense
        // Use addGroupExpense() - participants auto-resolved from group members
        // ===================================================================

        System.out.println("\n============= PATTERN B: GROUP-BASED EXPENSES =============");

        // ===== 5. Percentage Split (group-based) =====
        System.out.println("--- Step 5B: Percentage Split (Cab $50 - Weekend Trip group) ---");
        // $50 cab: Alice 40%, Bob 30%, Charlie 20%, Diana 10%
        // Participants are automatically resolved from group 'g1' members
        splitWise.addGroupExpense(
                "e3", "Cab Ride", 50.0, "u3",                 // Charlie paid
                "g1",                                           // Group ID - auto-resolves all 4 members
                SplitType.PERCENTAGE, Arrays.asList(40.0, 30.0, 20.0, 10.0)
        );
        splitWise.printBalances();

        // ===== 6. Check Net Balances =====
        System.out.println("--- Step 6: Net Balance Verification ---");
        System.out.println("  Alice owes Bob: $" + String.format("%.2f", splitWise.getBalance("u1", "u2")));
        System.out.println("  Bob owes Alice: $" + String.format("%.2f", splitWise.getBalance("u2", "u1")));
        System.out.println("  (These are inverse: $" + String.format("%.2f", splitWise.getBalance("u1", "u2"))
                + " = -($" + String.format("%.2f", splitWise.getBalance("u2", "u1")) + "))");
        System.out.println("  Charlie owes Alice: $" + String.format("%.2f", splitWise.getBalance("u3", "u1")));
        System.out.println("  Diana owes Charlie: $" + String.format("%.2f", splitWise.getBalance("u4", "u3")));

        // ===== 7. Another group-based expense (Equal split) =====
        System.out.println("\n--- Step 7B: Group-based Equal Split (Groceries $10 - Weekend Trip group) ---");
        // $10 groceries equally split among all group members
        // Since paidBy (Diana) is in the group, they are excluded from owing themselves
        splitWise.addGroupExpense(
                "e4", "Groceries", 10.0, "u4",                // Diana paid
                "g1",                                           // Group ID - auto-resolves all 4 members
                SplitType.EQUAL, null                           // Equal split ($3.34, $3.33, $3.33)
        );
        System.out.println("  $10 split equally among 4 group members (Diana paid, so 3 others owe):");
        splitWise.printBalances();

        // ===== 8. Partial Settlement =====
        System.out.println("\n============= PARTIAL & FULL SETTLEMENTS =============");
        System.out.println("--- Step 8: Partial Settlement ---");
        double aliceOwesBob = splitWise.getBalance("u1", "u2");
        System.out.println("  Alice owes Bob: $" + String.format("%.2f", aliceOwesBob));
        double partialPayment = Math.round(aliceOwesBob / 2.0 * 100.0) / 100.0;
        System.out.println("  Alice pays half: $" + String.format("%.2f", partialPayment) + " (partial).");
        splitWise.settleUp("s1", "u1", "u2", partialPayment);
        splitWise.printBalances();

        // ===== 9. Full Settlement =====
        System.out.println("--- Step 9: Full Settlement ---");
        double remainingOwed = splitWise.getBalance("u1", "u2");
        System.out.println("  Remaining owed by Alice: $" + String.format("%.2f", remainingOwed));
        if (remainingOwed > 0) {
            splitWise.settleUp("s2", "u1", "u2", remainingOwed);
        }
        splitWise.printBalances();

        // ===== 10. Verify Notifications =====
        System.out.println("--- Step 10: Notification Summary ---");
        var expenseNotifs = splitWise.getNotificationService().getExpenseNotifications();
        var settlementNotifs = splitWise.getNotificationService().getSettlementNotifications();
        System.out.println("  Total expense notifications: " + expenseNotifs.size());
        System.out.println("  Total settlement notifications: " + settlementNotifs.size());

        System.out.println("\n========================================");
        System.out.println("  DEMO COMPLETED SUCCESSFULLY");
        System.out.println("========================================");
    }
}