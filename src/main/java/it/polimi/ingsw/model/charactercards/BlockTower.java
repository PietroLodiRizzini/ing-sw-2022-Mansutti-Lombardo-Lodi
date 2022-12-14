package it.polimi.ingsw.model.charactercards;

import it.polimi.ingsw.model.Game;

public class BlockTower extends CharacterCard {
    public BlockTower(Game currentGame) {
        this.currentGame = currentGame;
        cost = 3;

        type = CharacterCardType.BLOCK_TOWER;
        name = "BlockTower";
        description = "When resolving a Conquering on an island, Towers do not count towards influence.";
    }

    /**
     * Set true the boolean blockTower of IslandGroup
     */
    public void doEffect() {

        currentGame.getPlayers().get(currentGame.getCurrentPlayer()).setCoins(currentGame.getPlayers().get(currentGame.getCurrentPlayer()).getCoins() - cost);
        cost = 4;
        currentGame.setBlockTower(true);
    }
}
