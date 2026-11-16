public class ThreeSixesRule implements TurnRule {
    @Override
    public void onRoll(Player player, int diceValue) {
        if (diceValue == 6) {
            player.setConsecutiveSixes(player.getConsecutiveSixes() + 1);
        }else{
            player.setConsecutiveSixes(0);
        }
    }
    @Override
    public boolean shouldExtraTurnGive(Player player, int diceValue) {
        return diceValue == 6 && player.getConsecutiveSixes() < 3;
    }
    @Override
    public boolean shouldCancelTurn(Player player) {
        return player.getConsecutiveSixes() == 3;

    }
    @Override
    public void onTurnEnd(Player player) {
        player.setConsecutiveSixes(0);
    }
}
