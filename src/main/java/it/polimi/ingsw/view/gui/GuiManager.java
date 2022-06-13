package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Wizard;
import it.polimi.ingsw.model.reduced.ReducedGame;
import it.polimi.ingsw.model.reduced.ReducedPlayer;
import it.polimi.ingsw.observer.ViewObservable;
import it.polimi.ingsw.observer.ViewObserver;
import it.polimi.ingsw.view.View;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class GuiManager extends ViewObservable implements View{
    private final Parent nicknameRoot;
    private final Parent gameInfoRoot;
    private final Parent wizardRoot;
    private final Parent lobbyRoot;
    private final Parent tableRoot;

    private final ConnectToServerController connectToServerController;
    private final NicknameController nicknameController;
    private final GameInfoController gameInfoController;
    private final WizardController wizardController;
    private final LobbyController lobbyController;
    private final TableController tableController;

    private static Semaphore semaphore;
    private String nickname;

    public GuiManager() {
        semaphore = new Semaphore(0);

        // wait for graphics to be initialized
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FXMLLoader nicknameLoader = new FXMLLoader(getClass().getResource("/fxml/nickname.fxml"));
        FXMLLoader gameInfoLoader = new FXMLLoader(getClass().getResource("/fxml/gameInfo.fxml"));
        FXMLLoader wizardLoader = new FXMLLoader(getClass().getResource("/fxml/wizard.fxml"));
        FXMLLoader lobbyLoader = new FXMLLoader(getClass().getResource("/fxml/lobby.fxml"));
        FXMLLoader tableLoader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));

        try {
            nicknameRoot = nicknameLoader.load();
            gameInfoRoot = gameInfoLoader.load();
            wizardRoot = wizardLoader.load();
            lobbyRoot = lobbyLoader.load();
            tableRoot = tableLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        connectToServerController = Gui.getConnectToServerController();
        nicknameController = nicknameLoader.getController();
        gameInfoController = gameInfoLoader.getController();
        wizardController = wizardLoader.getController();
        lobbyController = lobbyLoader.getController();
        tableController = tableLoader.getController();

        System.out.println(tableController);

        gameInfoController.setGuiManager(this);
        lobbyController.setGuiManager(this);
        tableController.setGuiManager(this);

        Platform.setImplicitExit(false);
    }

    @Override
    public void addObserver(ViewObserver viewObserver) {
        super.addObserver(viewObserver);

        connectToServerController.addObserver(viewObserver);
        nicknameController.addObserver(viewObserver);
        wizardController.addObserver(viewObserver);
        gameInfoController.addObserver(viewObserver);
        tableController.addObserver(viewObserver);
    }

    public static Semaphore getSemaphore() {
        return semaphore;
    }

    @Override
    public void showConnectionOutcome(boolean connectionOk) {
        if (connectionOk) {
            Platform.runLater(() -> Gui.getStage().setScene(new Scene(nicknameRoot)));
        } else {
            connectToServerController.onError();
        }
    }

    @Override
    public void showLoginOutcome(boolean userNameAvailable, boolean newGame, String nickname, List<Wizard> availableWizards) {
        if (userNameAvailable) {
            if (newGame) {
                // ask parameters for the new game
                Platform.runLater(() -> Gui.getStage().setScene(new Scene(gameInfoRoot)));
            } else {
                // ask wizard
                wizardController.setAvailableWizards(availableWizards);
                askWizard();
            }
            this.nickname = nickname;
        } else {
            nicknameController.onError();
        }
    }

    public void askWizard() {
        Platform.runLater(() -> Gui.getStage().setScene(new Scene(wizardRoot)));
    }

    @Override
    public void updateAvailableWizards(List<Wizard> availableWizards) {
        wizardController.setAvailableWizards(availableWizards);
    }

    @Override
    public void showWizardError(List<Wizard> availableWizards) {
        System.out.println("Wizard error");
        wizardController.setAvailableWizards(availableWizards);
        wizardController.onError();
    }

    @Override
    public void showLobby(List<ReducedPlayer> players, int playersNumber) {
        System.out.println("show lobby " + players.size() + playersNumber);


        Platform.runLater(() -> {
            if(! Gui.getStage().getScene().getRoot().equals(lobbyRoot))
                Gui.getStage().setScene(new Scene(lobbyRoot));

            lobbyController.setPlayersNumber(playersNumber);
            lobbyController.setPlayers(players);
        });
    }

    @Override
    public void askAssistantCard(Map<Integer, Integer> hand, List<Integer> notPlayable) {
        tableController.askAssistantCard(hand, notPlayable);
    }

    @Override
    public void showPlayedAssistantCard(String player, int card) {

    }

    @Override
    public void update(ReducedGame game) {
        System.out.println("update received");
        tableController.update(game);
    }

    @Override
    public void askActionPhase1(int count, int maxIsland, boolean expert) {
        Platform.runLater(() -> tableController.askActionPhase1(count));
    }

    @Override
    public void askActionPhase2(int maxMNStpes, boolean expert) {
        Platform.runLater(() -> tableController.askActionPhase2(maxMNStpes));
    }

    @Override
    public void askActionPhase3(List<Integer> alloweValues, boolean expert) {

    }

    @Override
    public void askCCAllRemoveColorInput() {

    }

    @Override
    public void askCCBlockColorOnceInput() {

    }

    @Override
    public void askCCChoose1DiningRoomInput(List<Color> allowedValues) {

    }

    @Override
    public void askCCChoose1ToIslandInput(List<Color> allowedColors, int maxIsland) {

    }

    @Override
    public void askCCChoose3ToEntranceInput(List<Color> allowedFromCC, List<Color> allowedFromEntrance) {

    }

    @Override
    public void askCCChooseIslandInput(int maxIsland) {

    }

    @Override
    public void askCCExchange2StudentsInput(List<Color> entrance, List<Color> diningRoom) {

    }

    @Override
    public void askCCNoEntryIslandInput(int maxIsland) {

    }

    @Override
    public void showStringMessage(String content) {
        tableController.showMessage(content);
    }

    @Override
    public void shutdown(String message) {

    }

    @Override
    public void showServerUnreachable() {

    }

    public void showTable() {
        Platform.runLater(() -> Gui.getStage().setScene(new Scene(tableRoot)));
    }

    public String getNickname() {
        return nickname;
    }
}