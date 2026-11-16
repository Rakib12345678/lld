package com.rocky.splitwise.split;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Expense;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExactSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Double> calculateSplitAmounts(Expense expense) {
        double amount = expense.getAmount();
        var participants = expense.getParticipants();
        var exactAmounts = expense.getSplitValues();

        if (exactAmounts == null || exactAmounts.size() != participants.size()) {
            throw new SplitWiseException("Exact split requires split values matching participant count");
        }

        double total = exactAmounts.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - amount) > 0.01) {
            throw new SplitWiseException("Exact split amounts sum to " + total
                    + " but expense amount is " + amount);
        }

        Map<String, Double> splitMap = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            splitMap.put(participants.get(i).getId(), exactAmounts.get(i));
        }
        return splitMap;
    }

    @Override
    public void validate(Expense expense) {
        if (expense.getSplitValues() == null) {
            throw new SplitWiseException("Exact split requires split values");
        }
        if (expense.getSplitValues().size() != expense.getParticipants().size()) {
            throw new SplitWiseException("Number of split values must match number of participants");
        }
        for (Double val : expense.getSplitValues()) {
            if (val < 0) {
                throw new SplitWiseException("Split value cannot be negative");
            }
        }
    }
}