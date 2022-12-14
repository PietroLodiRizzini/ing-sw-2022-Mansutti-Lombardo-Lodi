package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.FullDiningRoomException;
import it.polimi.ingsw.exceptions.MissingStudentException;

import java.util.*;

public class SchoolDashboard {
    private final ArrayList<Color> professors;
    private int towers;
    private EnumMap<Color,Integer> entrance;
    private final EnumMap<Color,Integer> diningRoom;
    private Game currentGame;

    /**
     * Sets the numbers of towers and initialize the entrance and the dining room
     * @param currentGame
     */
    SchoolDashboard(Game currentGame)
    {
        professors= new ArrayList<>();

        this.currentGame = currentGame;

        if(currentGame.getMaxPlayers() == 2)
            towers = 8;
        else
            towers = 6;

        entrance = new EnumMap<>(Color.class);
        diningRoom = new EnumMap<>(Color.class);

        Arrays.stream(Color.values()).forEach(color -> {
            entrance.putIfAbsent(color, 0);
            diningRoom.putIfAbsent(color, 0);
        });
    }

    /**
     * Initialise the entrance of players with: seven student if it's a 2 players game, nine else
     */
    public void setUp() {
        if (currentGame.getMaxPlayers() == 2)
            entrance = currentGame.extractFromBag(7);
        else
            entrance = currentGame.extractFromBag(9);
    }

    /**
     * Adds n towers to the school dashboard
     * @param n number of towers
     */
    public void addTowers(int n)
    {
        if(n<0 || towers+n>8) throw new IllegalArgumentException("tower number not valid");
        towers = towers + n;
    }

    /**
     * Remove n towers from the school dashboard
     * @param n number of towers
     */
    public void removeTowers(int n)
    {
        towers = towers - n;
    }

    public int getTowers()
    {
        return towers;
    }

    public EnumMap<Color, Integer> getEntrance() {
        return entrance;
    }

    /**
     * Converts the entrance into a list of color
     * @return a list of color
     */
    public List<Color> entranceAsList() {
        List<Color> res = new ArrayList<>();

        for (Color c : entrance.keySet()) {
            for (int i = 0; i < entrance.get(c); i++)
                res.add(c);
        }

        return res;
    }

    /**
     * Removes a student from the entrance
     * @param color student color
     */
    public void removeStudentFromEntrance(Color color) throws MissingStudentException {
        entrance.putIfAbsent(color, 0);
        if(entrance.get(color)==0) throw new MissingStudentException();
        entrance.put(color,entrance.get(color)-1);
    }

    /**
     * Adds a student to the entrance
     * @param color student color
     * @throws NullPointerException if color is null
     */
    public void addStudentToEntrance(Color color) throws NullPointerException
    {
        entrance.putIfAbsent(color, 0);
        entrance.put(color,entrance.get(color)+1);
    }

    /**
     * Increments the amount of student in entrance of the specified color by quantity
     * @param color color of students to add
     * @param quantity how many students to add
     */
    public void addStudentsToEntrance(Color color, int quantity) {
        for (int i = 0; i < quantity; i++)
            addStudentToEntrance(color);
    }


    public EnumMap<Color, Integer> getDiningRoom()
    {
        return diningRoom;
    }

    public int getDiningRoom(Color color) {
        return diningRoom.getOrDefault(color, 0);
    }

    /**
     * Converts the dining room into a list of color
     * @return a list of color
     */

    public List<Color> diningRoomAsList() {
        List<Color> res = new ArrayList<>();

        for (Color c : diningRoom.keySet()) {
            for (int i = 0; i < diningRoom.get(c); i++)
                res.add(c);
        }

        return res;
    }

    /**
     * Moves student from entrance to dining room
     * @param color student color
     * @throws NullPointerException if color is null
     * @throws FullDiningRoomException if the dining room already contains the maximum (10) number of students
     */
    public void moveStudentToDiningRoom(Color color) throws NullPointerException, MissingStudentException, FullDiningRoomException {
        if (diningRoom.getOrDefault(color, 0) >= 10)
            throw new FullDiningRoomException();

        removeStudentFromEntrance(color);
        addStudentToDiningRoom(color);
    }

    /**
     * Adds a student to the Dining Room, and if the students of that color in that dining room, are more than the other players, move a professor.
     *
     * @param color student color
     * @throws NullPointerException if color is null
     */
    public void addStudentToDiningRoom(Color color) throws NullPointerException
    {
        boolean hasMoreStudents = true;
        diningRoom.putIfAbsent(color, 0);
        diningRoom.put(color, diningRoom.get(color)+1);
        if(currentGame.isExpertMode()){
            if(diningRoom.get(color) % 3 == 0)
                currentGame.getCurrentPlayerInstance().addCoins();
        }


        if(currentGame.getCurrentPlayerInstance().getSchoolDashboard().getProfessors().contains(color)) return;

        //for each player different from the current player, check the number of students in his dining room, if they are less then add a professor to currentplayer
        for(int i=0; i<currentGame.getPlayers().size();i++)
        {
            if(i != currentGame.getCurrentPlayer())
            {
                currentGame.getPlayers().get(i).getSchoolDashboard().getDiningRoom().putIfAbsent(color, 0);

                //checks all players, if any of them has a number of students of the chosen color larger than the current player, sets hasMoreStudents to false

                if(currentGame.getCurrentPlayerInstance().getSchoolDashboard().getDiningRoom().get(color)<=
                        currentGame.getPlayers().get(i).getSchoolDashboard().getDiningRoom().get(color))
                {
                    hasMoreStudents=false;
                }
                //if player i has less students and has the professor, move the professor
                if(currentGame.getPlayers().get(i).getSchoolDashboard().getProfessors().contains(color) && hasMoreStudents)
                {
                    currentGame.getCurrentPlayerInstance().getSchoolDashboard().addProfessor(color);
                    currentGame.getPlayers().get(i).getSchoolDashboard().removeProfessor(color);
                    hasMoreStudents=false;
                }

            }
        }
        //if no player has more students than the current player, and no player has the chosen professor color, remove a professor from current game
        if(hasMoreStudents)
        {
            currentGame.getCurrentPlayerInstance().getSchoolDashboard().addProfessor(color);
            currentGame.removeUnusedProfessor(color);
        }

    }

    /**
     * Removes a student from dining room.
     * @param color color of the student to remove.
     */
    public void removeStudentFromDiningRoom(Color color) {
        diningRoom.putIfAbsent(color,0);
        diningRoom.put(color, diningRoom.get(color) - 1);
    }

    /**
     * Adds a professor to the player's school dashboard
     * @param color professor color
     * @throws IllegalArgumentException if there is already a professor of that color
     * @throws NullPointerException if color is null
     */
    public void addProfessor(Color color) throws NullPointerException
    {
        professors.add(color);
    }


    public ArrayList<Color> getProfessors()
    {
        return professors;
    }


    /**
     * removes a professor from the school dashboard
     * @param color professor color
     * @throws NullPointerException if color is null
     * @throws IllegalArgumentException if there is no professor of the chosen color
     */

    public void removeProfessor(Color color) throws NullPointerException
    {
        if(!professors.contains(color)) throw new IllegalArgumentException("There is no professor of such color");
        professors.remove(color);
    }

    /**
     *
     * @param color of the student to me moved
     * @param islandIndex islandGroup number
     * @throws NullPointerException if color is null
     * @throws IllegalArgumentException if there is no specified color in the entrance
     * @throws IndexOutOfBoundsException if islandIndex is out of range
     */
    public void moveToIslandGroup(Color color, int islandIndex) throws NullPointerException, MissingStudentException, IndexOutOfBoundsException
    {
        removeStudentFromEntrance(color);
        currentGame.getIslands().get(islandIndex).addStudents(color);
    }

    /**
     * Allows to check whether the school-dashboard has control over a professor or not.
     * @param color Color of the professor to check.
     * @return True if the professor is controlled by the schoolDashboard, false otherwise.
     */
    public Boolean hasProfessor(Color color) {
        return professors.contains(color);
    }

    public void setTowers(int towers) {
        this.towers = towers;
    }
}