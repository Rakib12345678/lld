package com.rocky.splitwise.split;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Expense;

import java.util.Map;

public interface SplitStrategy {
    /**
     * Calculates how much each participant owes.
     * Returns a Map of userId -> amount owed.
     */
    Map<String, Double> calculateSplitAmounts(Expense expense) throws SplitWiseException;
    void validate(Expense expense) throws SplitWiseException;
}