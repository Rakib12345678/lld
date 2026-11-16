package com.rocky.splitwise.split;

import com.rocky.splitwise.exception.SplitWiseException;
import com.rocky.splitwise.model.SplitType;

import java.util.HashMap;
import java.util.Map;

public class SplitStrategyFactory {
    private final Map<SplitType, SplitStrategy> strategies = new HashMap<>();

    public SplitStrategyFactory() {
        strategies.put(SplitType.EQUAL, new EqualSplitStrategy());
        strategies.put(SplitType.EXACT, new ExactSplitStrategy());
        strategies.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());
    }

    public SplitStrategy getStrategy(SplitType splitType) {
        SplitStrategy strategy = strategies.get(splitType);
        if (strategy == null) {
            throw new SplitWiseException("No strategy found for split type: " + splitType);
        }
        return strategy;
    }

    public void registerStrategy(SplitType splitType, SplitStrategy strategy) {
        strategies.put(splitType, strategy);
    }
}