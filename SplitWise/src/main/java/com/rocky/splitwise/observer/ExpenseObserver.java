package com.rocky.splitwise.observer;

import com.rocky.splitwise.model.Expense;

public interface ExpenseObserver {
    void onExpenseAdded(Expense expense);
}