package it.polimi.ingsw.model.charactercards;

import it.polimi.ingsw.exceptions.MissingStudentException;
import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Game;

import java.util.EnumMap;

public class Choose3toEntrance extends CharacterCard {

    public Choose3toEntrance(Game currentGame) {
        this.currentGame = currentGame;
        students = new EnumMap<>(Color.class);
        cost = 1;

        type = CharacterCardType.CHOOSE_3_TO_ENTRANCE;
        name = "Choose3toEntrance";
        description = "You may take up to 3 Students from his card and replace them with the same number of Students from your Entrance";

        init();
    }

    public void init() {
        students = currentGame.extractFromBag(6);
    }


    /**
     * Check that the number of selected students is the same and then move the students from the card to the entrance
     *
     * @param chosenFromCard     represent the students chosen from the card
     * @param chosenFromEntrance represent the students chosen from the entrance
     */

    public void doEffect(EnumMap<Color, Integer> chosenFromCard, EnumMap<Color, Integer> chosenFromEntrance) throws MissingStudentException {
        currentGame.getPlayers().get(currentGame.getCurrentPlayer()).setCoins(currentGame.getPlayers().get(currentGame.getCurrentPlayer()).getCoins() - cost);

        cost = 2;
        EnumMap<Color, Integer> support1 = chosenFromCard.clone();
        EnumMap<Color, Integer> support2 = chosenFromEntrance.clone();

        if (totalNumberofStudent(support1) != totalNumberofStudent(support2))
            throw new IllegalArgumentException("different number of selected students");


        for (Color c : chosenFromCard.keySet()) {

            while (chosenFromCard.get(c) > 0) {

                chosenFromCard.put(c, chosenFromCard.get(c) - 1);
                students.put(c, students.get(c) - 1);
                currentGame.getPlayers().get(currentGame.getCurrentPlayer()).getSchoolDashboard().addStudentToEntrance(c);
            }

        }
        for (Color c : chosenFromEntrance.keySet()) {
            while (chosenFromEntrance.get(c) > 0) {
                chosenFromEntrance.put(c, chosenFromEntrance.get(c) - 1);
                if (students.get(c) == null)
                    students.put(c, 1);
                else
                    students.put(c, students.get(c) + 1);
                currentGame.getPlayers().get(currentGame.getCurrentPlayer()).getSchoolDashboard().removeStudentFromEntrance(c);
            }


        }
    }

    public EnumMap<Color, Integer> getStudents() {
        return students;
    }

    /**
     * Calculate the total number of students
     *
     * @param e is the EnumMap
     * @return total number of students
     */
    public int totalNumberofStudent(EnumMap<Color, Integer> e) {
        int sum = 0;

        for (Color c : e.keySet()) {
            while (e.get(c) > 0) {
                e.put(c, e.get(c) - 1);
                sum++;
            }
        }
        return sum;
    }

}