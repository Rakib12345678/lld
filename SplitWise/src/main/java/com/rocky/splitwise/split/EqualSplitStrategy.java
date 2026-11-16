package com.rocky.splitwise.split;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Expense;

import java.util.LinkedHashMap;
import java.util.Map;

public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Double> calculateSplitAmounts(Expense expense) {
        int numParticipants = expense.getParticipants().size();
        double amount = expense.getAmount();
        double baseAmount = Math.floor(amount * 100 / numParticipants) / 100;
        double remainder = Math.round((amount - baseAmount * numParticipants) * 100.0) / 100.0;

        Map<String, Double> splitMap = new LinkedHashMap<>();
        var participants = expense.getParticipants();

        for (int i = 0; i < numParticipants; i++) {
            if (i == numParticipants - 1) {
                // Last person absorbs the rounding difference
                splitMap.put(participants.get(i).getId(),
                        Math.round((baseAmount + remainder) * 100.0) / 100.0);
            } else {
                splitMap.put(participants.get(i).getId(), baseAmount);
            }
        }

        // Validate total matches
        double total = splitMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - amount) > 0.01) {
            throw new SplitWiseException("Equal split rounding error: total " + total + " != " + amount);
        }

        return splitMap;
    }

    @Override
    public void validate(Expense expense) {
        if (expense.getParticipants() == null || expense.getParticipants().isEmpty()) {
            throw new SplitWiseException("Participants list cannot be empty for equal split");
        }
        if (expense.getAmount() < 0) {
            throw new SplitWiseException("Amount cannot be negative");
        }
    }
}