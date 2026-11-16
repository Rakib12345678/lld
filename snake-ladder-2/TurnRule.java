public interface TurnRule {
    default void onRoll(Player player, int diceValue) {

    }

    default boolean shouldExtraTurnGive(Player player,int diceValue) {
        return false;
    }

    default boolean shouldCancelTurn(Player player) {
        return false;
    }

    default void onTurnEnd(Player player) {}
}
