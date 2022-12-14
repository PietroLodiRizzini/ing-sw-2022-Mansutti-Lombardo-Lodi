package it.polimi.ingsw.model.charactercards;

import it.polimi.ingsw.model.Game;

public class NoEntryIsland extends CharacterCard {
    private int noEntryTiles;

    public NoEntryIsland(Game currentGame) {
        this.currentGame = currentGame;
        noEntryTiles = 4;
        cost = 2;

        type = CharacterCardType.NO_ENTRY_ISLAND;
        name = "NoEntryIsland";
        description = "Place a No Entry tile on an Island of you choice. The first time Mother Nature ends her movement there, put the No Entry tile back onto this card. Do not calculate influence on that island, or place any towers.";
    }

    /**
     * Set true the boolean noEntryIsland of IslandGroup
     */
    public void doEffect(int islNumb) {

        currentGame.getCurrentPlayerInstance().removeCoins(cost);

        cost = 3;

        if (noEntryTiles > 0) {

            noEntryTiles = noEntryTiles - 1;
            currentGame.getIslands().get(islNumb).addNoEntryTile();


        } else throw new IllegalArgumentException("Maximum number of effect uses");

    }

    public int getNoEntryTiles() {
        return noEntryTiles;
    }
    public void addNoEntryTile() {
        noEntryTiles++;
    }
}
