package com.rocky.splitwise.observer;

import com.rocky.splitwise.model.Settlement;

public interface SettlementObserver {
    void onSettlementMade(Settlement settlement);
}