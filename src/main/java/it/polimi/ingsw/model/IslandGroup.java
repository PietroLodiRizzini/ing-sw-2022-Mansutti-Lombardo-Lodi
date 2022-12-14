package it.polimi.ingsw.model;

import it.polimi.ingsw.model.charactercards.CharacterCardType;
import it.polimi.ingsw.model.charactercards.NoEntryIsland;

import java.util.EnumMap;

public class IslandGroup {
    private int islandCount = 1;
    private Player occupiedBy;
    private final EnumMap<Color, Integer> students = new EnumMap<>(Color.class);
    private int noEntryTiles = 0;
    private final Game game;

    /**
     * Island constructor. Initializes the island with 0 students for each color
     */

    public IslandGroup(Game game) {
        for (Color c : Color.values())
            students.put(c, 0);

        this.game = game;
    }

    /**
     * increases the number of students of the given color by the given quantity
     * @param color color of the students to add
     * @param quantity quantity by which increase students
     */
    public void addStudents(Color color, int quantity) {
        int prevVal = students.get(color) == null ? 0 : students.get(color);
        students.put(color, prevVal + quantity);
    }

    /**
     * Adds one student of the color chosen
     * @param color color of the student
     */
    public void addStudents(Color color) {
        addStudents(color, 1);
    }

    public int getStudents(Color color) {
        return students.get(color);
    }

    public EnumMap<Color,Integer> getStudents(){return students;}

    public int getIslandCount() {
        return islandCount;
    }

    /**
     * Increase the dimension of the island 
     * @param quantity
     */
    public void incrementIslandCount(int quantity) {
        islandCount += quantity;
    }

    public void setOccupiedBy(Player player) {
        occupiedBy = player;
    }

    public Player getOccupiedBy() {return occupiedBy;}


    public int getNoEntryTiles() {
        return noEntryTiles;
    }

    public boolean isNoEntry() {
        return noEntryTiles > 0;
    }


    public void addNoEntryTile() {
        noEntryTiles++;
    }

    public void removeNoEntryTile() {
        ((NoEntryIsland) game.getCharacterCard(CharacterCardType.NO_ENTRY_ISLAND)).addNoEntryTile();

        noEntryTiles--;
    }


    /**
     * Merges th current island with another given one.
     * @param with Island to merge. It is supposed to have the same owner as the current island.
     */
    public void merge(IslandGroup with) {
        noEntryTiles += with.noEntryTiles;

        for(Color c : Color.values())
            addStudents(c, with.getStudents(c));

        incrementIslandCount(with.getIslandCount());
    }

    public void resolve() {
        Player higherInfluence = game.playerWithHigherInfluence(this);
        if (islandCount == 0) {
            if (higherInfluence != null) {
                higherInfluence.getSchoolDashboard().removeTowers(1);
                setOccupiedBy(higherInfluence);
            }
        } else {
            if (! higherInfluence.equals(occupiedBy)) {
                occupiedBy.getSchoolDashboard().addTowers(islandCount);
                higherInfluence.getSchoolDashboard().removeTowers(islandCount);
            }
        }
    }
}
