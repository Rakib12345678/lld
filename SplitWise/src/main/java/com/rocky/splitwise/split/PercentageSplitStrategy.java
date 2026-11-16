package com.rocky.splitwise.split;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.Expense;

import java.util.LinkedHashMap;
import java.util.Map;

public class PercentageSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Double> calculateSplitAmounts(Expense expense) {
        double amount = expense.getAmount();
        var participants = expense.getParticipants();
        var percentages = expense.getSplitValues();

        if (percentages == null || percentages.size() != participants.size()) {
            throw new SplitWiseException("Percentage split requires percentage values matching participant count");
        }

        double totalPercentage = percentages.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalPercentage - 100.0) > 0.01) {
            throw new SplitWiseException("Percentages must sum to 100, but got " + totalPercentage);
        }

        Map<String, Double> splitMap = new LinkedHashMap<>();

        // Calculate amounts based on percentages
        for (int i = 0; i < participants.size(); i++) {
            double splitAmount = Math.round(amount * percentages.get(i) / 100.0 * 100.0) / 100.0;
            splitMap.put(participants.get(i).getId(), splitAmount);
        }

        // Handle rounding: adjust the last participant
        double calculatedTotal = splitMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double diff = Math.round((amount - calculatedTotal) * 100.0) / 100.0;
        if (Math.abs(diff) > 0.01) {
            String lastUserId = participants.get(participants.size() - 1).getId();
            splitMap.put(lastUserId, Math.round((splitMap.get(lastUserId) + diff) * 100.0) / 100.0);
        }

        // Final validation
        double finalTotal = splitMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(finalTotal - amount) > 0.01) {
            throw new SplitWiseException("Percentage split rounding error: total " + finalTotal + " != " + amount);
        }

        return splitMap;
    }

    @Override
    public void validate(Expense expense) {
        if (expense.getSplitValues() == null) {
            throw new SplitWiseException("Percentage split requires split values");
        }
        if (expense.getSplitValues().size() != expense.getParticipants().size()) {
            throw new SplitWiseException("Number of percentage values must match number of participants");
        }
        for (Double val : expense.getSplitValues()) {
            if (val < 0 || val > 100) {
                throw new SplitWiseException("Percentage value must be between 0 and 100");
            }
        }
    }
}