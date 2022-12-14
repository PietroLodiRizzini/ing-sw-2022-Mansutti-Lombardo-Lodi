package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.MissingStudentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchoolDashboardTest
{
    Game game;
    @BeforeEach
    public void setUp()
    {
        game = new Game(2, true);
        game.addPlayer("p1");
        game.addPlayer("p2");
        game.addPlayer("p3");

    }
    @Test
    public void RemoveTowersTest()
    {

        game.getPlayers().get(1).getSchoolDashboard().removeTowers(3);
        Assertions.assertEquals(5, game.getPlayers().get(1).getSchoolDashboard().getTowers());
    }

    @Test
    public void addTowersTest()
    {

        game.getPlayers().get(1).getSchoolDashboard().removeTowers(3);
        game.getPlayers().get(1).getSchoolDashboard().addTowers(1);
        Assertions.assertEquals(6, game.getPlayers().get(1).getSchoolDashboard().getTowers());
    }

    @Test
    public void addTowersShouldThrowRuntimeExceptionWhenMoreThan8Test()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> game.getPlayers().get(1).getSchoolDashboard().addTowers(1));
    }

    @Test
    public void addTowersShouldThrowRuntimeExceptionWhenNegativeTest()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> game.getPlayers().get(1).getSchoolDashboard().addTowers(-1));
    }

    @Test
    public void addStudentToEntranceTest()
    {
        game.getPlayers().get(1).getSchoolDashboard().getEntrance().clear();
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.YELLOW);
        Assertions.assertEquals(1, game.getPlayers().get(1).getSchoolDashboard().getEntrance().get(Color.YELLOW));
    }
    @Test
    public void addStudentToEntranceShouldThrowRuntimeExceptionWhenNullTest()
    {
        Assertions.assertThrows(NullPointerException.class, () -> game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(null));
    }

    @Test
    public void removeStudentFromEntranceTest() throws MissingStudentException {
        game.getPlayers().get(1).getSchoolDashboard().getEntrance().clear();
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.YELLOW);
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.RED);
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.RED);
        game.getPlayers().get(1).getSchoolDashboard().removeStudentFromEntrance(Color.YELLOW);
        game.getPlayers().get(1).getSchoolDashboard().removeStudentFromEntrance(Color.RED);
        Assertions.assertEquals(0, game.getPlayers().get(1).getSchoolDashboard().getEntrance().get(Color.YELLOW));
        Assertions.assertEquals(1, game.getPlayers().get(1).getSchoolDashboard().getEntrance().get(Color.RED));
    }

    @Test
    public void removeStudentFromEntranceShouldThrowRuntimeExceptionWhenNullTest()
    {
        Assertions.assertThrows(NullPointerException.class, () -> game.getPlayers().get(1).getSchoolDashboard().removeStudentFromEntrance(null));
    }
    @Test
    public void addStudentToEntrance()
    {
        game.getPlayers().get(1).getSchoolDashboard().getEntrance().clear();
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.YELLOW);
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.RED);
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.RED);
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.YELLOW);
        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.RED);
        Assertions.assertEquals(2, game.getPlayers().get(1).getSchoolDashboard().getEntrance().get(Color.YELLOW));
        Assertions.assertEquals(3, game.getPlayers().get(1).getSchoolDashboard().getEntrance().get(Color.RED));
    }

    @Test
    public void moveStudentToDiningRoom() throws MissingStudentException {
        game.setCurrentPlayer(1);
        game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().getEntrance().clear();

        game.getPlayers().get(1).getSchoolDashboard().addStudentToEntrance(Color.YELLOW);
        game.getPlayers().get(1).getSchoolDashboard().moveStudentToDiningRoom(Color.YELLOW);

        Assertions.assertEquals(0, game.getPlayers().get(1).getSchoolDashboard().getEntrance().get(Color.YELLOW));
        Assertions.assertEquals(1, game.getPlayers().get(1).getSchoolDashboard().getDiningRoom().get(Color.YELLOW));

    }

    @Test
    public void addStudentToDiningRoom()
    {
        //one yellow student is added to player 1 dining room
        game.setCurrentPlayer(1);


        game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().getEntrance().clear();
        game.getPlayers().get(1).getSchoolDashboard().addStudentToDiningRoom(Color.YELLOW);

        //player 1 now has the yellow professor
        Assertions.assertTrue(game.getPlayers().get(1).getSchoolDashboard().getProfessors().contains(Color.YELLOW));

        //2 yellow students are added to player 2 dining room, professor is now moved to player2
        game.setCurrentPlayer(2);
        game.getCurrentPlayerInstance().setCoins(0);
        game.getPlayers().get(2).getSchoolDashboard().addStudentToDiningRoom(Color.YELLOW);
        Assertions.assertFalse(game.getPlayers().get(2).getSchoolDashboard().getProfessors().contains(Color.YELLOW));
        game.getPlayers().get(2).getSchoolDashboard().addStudentToDiningRoom(Color.YELLOW);
        Assertions.assertTrue(game.getPlayers().get(2).getSchoolDashboard().getProfessors().contains(Color.YELLOW));

        //adds one more student to player 2 dining room, checks if a coin is created
        game.getPlayers().get(2).getSchoolDashboard().addStudentToDiningRoom(Color.YELLOW);
        Assertions.assertEquals(1, game.getPlayers().get(2).getCoins());

    }

    @Test
    public void removeStudentFromDiningRoom(){
        //2 blue students are added for every player
        game.setCurrentPlayer(0);
        for(int i=0;i<2;i++){
            game.setCurrentPlayer(i);
            game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().addStudentToDiningRoom(Color.BLUE);
            game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().addStudentToDiningRoom(Color.BLUE);
        }

        assertTrue(game.getPlayers().get(0).getSchoolDashboard().getProfessors().contains(Color.BLUE));

        game.setCurrentPlayer(0);
        game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().removeStudentFromDiningRoom(Color.BLUE);

        Assertions.assertEquals(1, game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().getDiningRoom().get(Color.BLUE));

    }

    @Test
    public void moveToIslandGroup() throws MissingStudentException {
        game.setCurrentPlayer(0);
        game.getIslands().get(1).getStudents().clear();
        game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().addStudentToEntrance(Color.GREEN);
        game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().moveToIslandGroup(Color.GREEN,1);
        Assertions.assertEquals(0, game.getPlayers().get(game.getCurrentPlayer()).getSchoolDashboard().getDiningRoom().get(Color.GREEN));
        Assertions.assertEquals(1,game.getIslands().get(1).getStudents(Color.GREEN));
    }








}