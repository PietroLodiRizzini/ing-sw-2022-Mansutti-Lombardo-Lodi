package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.AlreadyUsedWizardException;
import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Wizard;
import it.polimi.ingsw.model.charactercards.*;
import it.polimi.ingsw.model.reduced.ReducedGame;
import it.polimi.ingsw.model.reduced.ReducedPlayer;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.observer.Observer;
import it.polimi.ingsw.view.VirtualView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the flow of the game serer side.
 */
public class GameController {
    private Game game;
    private final Map<String, VirtualView> nickVirtualViewMap;
    private GameState state;
    private int playerActionCount;
    private final int gameId;
    private ArrayList<Player> prio;

    public GameController(int playersNumber, boolean expertMode, int gameId)
    {
        this.game = new Game(playersNumber, expertMode);
        this.nickVirtualViewMap = Collections.synchronizedMap(new HashMap<>());
        this.state= new InitialState(this);
        this.gameId = gameId;
        prio = new ArrayList<>(game.getPlayers().size());
        playerActionCount=0;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Adds a player to the game.
     * @param nickname Player's nickname.
     * @param virtualView Player's virtualview.
     */
    public void addPlayer(String nickname, VirtualView virtualView) {
        nickVirtualViewMap.put(nickname, virtualView);
        game.addPlayer(nickname);
    }

    public ArrayList<Player> getPrio()
    {
        return prio;
    }

    /**
     * @return List of available wizards, that are the wizards not alreasy chosen by the players that joined the game.
     */
    public List<Wizard> getAvailableWizards() {
        List<Wizard> alreadyUsed = game.getPlayers().stream().map(Player::getWizard).toList();
        List<Wizard> wizards = new ArrayList<>(Arrays.stream(Wizard.values()).toList());
        wizards.removeAll(alreadyUsed);

        return wizards;
    }

    public int getMaxPlayers() {
        return game.getMaxPlayers();
    }

    /**
     * Changes state
     * @param state next state
     */
    public void changeState(GameState state)
    {
        System.out.println(gameLogHeader() + " new state: " + state.toString());
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public String gameLogHeader() {
        return "[Game" + getId() + "]";
    }

    public void log(String toLog) {
        System.out.println(gameLogHeader() + " " + toLog);
    }

    /**
     * when the game starts the state is change to planning 1 state
     */
    private void startGame()
    {
        if (game.getPlayersCount() != game.getMaxPlayers())
            return;

        state.startGame(); // state changes to Planning1State
        broadcastMessage("All players have joined.");
        state.planning1();
        askAssistantCard(null);
    }

    public Game getGame() {
        return game;
    }

    public int getId() {
        return gameId;
    }

    public VirtualView getVirtualView(String nickname) {
        return nickVirtualViewMap.get(nickname);
    }

    public VirtualView getCurrentPlayerView() {
        return getVirtualView(game.getCurrentPlayerNick());
    }

    /**
     * Dispatch messages received
     * @param message message
     */
    public void onMessageArrived(Message message)
    {
        switch (message.getMessageType()) {
            case FILL_CLOUD_CARDS -> state.planning1();
            case CHOOSE_WIZARD -> {
                ChooseWizard chooseWizardMsg = (ChooseWizard) message;
                handleChooseWizard(chooseWizardMsg.getNickname(), chooseWizardMsg.getWizard());
            }
            case PLAY_ASSISTANT_CARD -> {
                PlayAssistantCard msg = (PlayAssistantCard) message;
                state.planning2(msg.getChosenCard());
            }
            case MOVE_STUDENT_TO_ISLAND -> {
                MoveStudentToIsland msg2 = (MoveStudentToIsland) message;
                state.action1Island(msg2.getColor(), msg2.getIslandNumber());
            }
            case MOVE_STUDENT_TO_DINING_ROOM -> {
                MoveStudentToDiningRoom msg3 = (MoveStudentToDiningRoom) message;
                state.action1DiningRoom(msg3.getColor());
            }
            case MOVE_MOTHER_NATURE -> {
                MoveMotherNature msg4 = (MoveMotherNature) message;
                state.action2(msg4.getSteps());
            }
            case CHOOSE_CLOUD_CARD -> {
                ChooseCloudCard msg5 = (ChooseCloudCard) message;
                state.action3(msg5.getCloudCard());
            }
            case PLAY_CHARACTER_CARD -> {
                PlayCharacterCard playCharacterCard = (PlayCharacterCard) message;
                handleCharacterCardRequest(playCharacterCard.getChosenCard());
            }
            case CC_ALL_REMOVE_COLOR_REPLY -> {
                CCAllRemoveColorReply ccAllRemoveColorReply = (CCAllRemoveColorReply) message;
                playCCAllRemoveColor(ccAllRemoveColorReply.getColor());
            }
            case CC_BLOCK_COLOR_ONCE_REPLY -> {
                CCBlockColorOnceReply ccBlockColorOnceReply = (CCBlockColorOnceReply) message;
                playCCBlockColorOnce(ccBlockColorOnceReply.getColor());
            }
            case CC_CHOOSE_1_DINING_ROOM_REPLY -> {
                CCChoose1DiningRoomReply ccChoose1DiningRoomReply = (CCChoose1DiningRoomReply) message;
                playCCChoose1DiningRoom(ccChoose1DiningRoomReply.getColor());
            }
            case CC_CHOOSE_1_TO_ISLAND_REPLY -> {
                CCChoose1ToIslandReply ccChoose1ToIslandReply = (CCChoose1ToIslandReply) message;
                playCCChoose1ToIsland(ccChoose1ToIslandReply.getColor(), ccChoose1ToIslandReply.getIsland() - 1);
            }
            case CC_CHOOSE_3_TO_ENTRANCE_REPLY -> {
                CCChoose3ToEntranceReply choose3ToEntranceReply = (CCChoose3ToEntranceReply) message;
                playCCChoose3ToEntrance(choose3ToEntranceReply.getChosenFromCard(), choose3ToEntranceReply.getChosenFromEntrance());
            }
            case CC_CHOOSE_3_TO_ENTRANCE_PARTIAL_REPLY -> {
                CCChoose3ToEntrancePartialReply partialReply = (CCChoose3ToEntrancePartialReply) message;
                playCCChoose3ToEntrancePartialSwap(partialReply.getChosenFromCard(), partialReply.getChosenFromEntrance(), partialReply.getInputCount());
            }
            case CC_CHOOSE_3_TO_ENTRANCE_STOP, CC_EXCHANGE_2_STUDENTS_STOP -> {
                restoreGameFlow();
            }
            case CC_CHOOSE_ISLAND_REPLY -> {
                CCChooseIslandReply ccChooseIslandReply = (CCChooseIslandReply) message;
                playCCChooseIsland(ccChooseIslandReply.getChosenIsland());
            }
            case CC_EXCHANGE_2_STUDENTS_REPLY -> {
                CCExchange2StudentsReply ccExchange2StudentsReply = (CCExchange2StudentsReply) message;
                playCCExchange2Students(ccExchange2StudentsReply.getChosenFromEntrance(), ccExchange2StudentsReply.getChosenFromDiningRoom());
            }
            case CC_EXCHANGE_2_STUDENTS_PARTIAL_REPLY -> {
                CCExchange2StudentsPartialReply partialReply = (CCExchange2StudentsPartialReply) message;
                playCCExchange2StudentsPartialSwap(partialReply.getFromEntrance(), partialReply.getFromDiningRoom(), partialReply.getInputCount());
            }
            case CC_NO_ENTRY_ISLAND_REPLY -> {
                CCNoEntryIslandReply ccNoEntryIslandReply = (CCNoEntryIslandReply) message;
                playCCNoEntryIsland(ccNoEntryIslandReply.getIsland());
            }
            default -> throw new IllegalStateException("Protocol violation: unexpected " + message.getMessageType());
        }

    }

    /**
     * manages the request of wizard, updates the lobby
     * @param nickname
     * @param wizard
     */
    private void handleChooseWizard(String nickname, Wizard wizard) {
        try {
            System.out.println(gameLogHeader() + " " + nickname + " has requested wizard " + wizard);
            game.setPlayerWizard(nickname, wizard);

            // notify the change to all the players that haven't chosen a wizard yet
            game.getPlayers().stream().filter(p -> p.getWizard() == null)
                    .map(Player::getNickname)
                    .map(nickVirtualViewMap::get)
                    .forEach(v -> v.updateAvailableWizards(getAvailableWizards()));

            // update lobby for all players that have already chosen a wizard
            game.getPlayers().stream().filter(p -> p.getWizard() != null)
                    .map(Player::getNickname)
                    .map(nickVirtualViewMap::get)
                    .forEach(v -> v.showLobby(ReducedPlayer.list(game), getMaxPlayers()));
        } catch (AlreadyUsedWizardException e) {
            getVirtualView(nickname).showWizardError(getAvailableWizards());
        }

        boolean wizardOk = true;
        for (Player p : game.getPlayers())
            if (p.getWizard() == null)
                wizardOk = false;

        if (wizardOk)
            startGame();
    }

    /**
     * sets playerActionCount to 0
     */
    public void clearPlayerActionCount() {
        this.playerActionCount = 0;
    }

    /**
     * increments playerActionCount
     */
    public void addPlayerActionCount() {
        this.playerActionCount++;
    }

    public int getPlayerActionCount() {
        return playerActionCount;
    }

    /**
     * Allows to check if the game can still be joined (the maximum number of players has not been reached yet).
     * @return true if the game can be joined, false otherwise.
     */
    public boolean canBeJoined() {
        return (game.getPlayersCount() < game.getMaxPlayers());
    }

    /**
     * Broadcasts a string message to all the views.
     * @param content String message to display.
     */
    private void broadcastMessage(String content) {
        nickVirtualViewMap.values().forEach(virtualView -> virtualView.showStringMessage(content));
    }

    /**
     * Broadcasts a string message to all the views except the provided one.
     * @param content String message to display.
     */
    public void broadcastExceptCurrentPlayer(String content) {
        viewsExceptCurrentPlayer().forEach(virtualView -> virtualView.showStringMessage(content));
    }

    /**
     * Asks current Player to play an assistant card, providing possible values.
     * @param notPlayable List integers representing priorities of not playable character cards.
     */
    public void askAssistantCard(List<Integer> notPlayable) {
        notPlayable = notPlayable == null ? new ArrayList<>() : notPlayable;

        String currentNick = game.getCurrentPlayerNick();
        System.out.println("[Game" + gameId + "] asking assistantCard to " + currentNick);

        VirtualView currentView = getVirtualView(currentNick);
        currentView.askAssistantCard(game.getCurrentPlayerInstance().getHand().getAsMap(), notPlayable);


        broadcastMessage(currentNick + " is choosing an assistant card... ", currentNick);
    }

    /**
     * Broadcasts a message to the views, excluding the nickname provided.
     * @param content message to display.
     * @param excludedNick nickname to exclude from message delivery.
     */
    public void broadcastMessage(String content, String excludedNick) {
        viewsExcept(excludedNick).forEach(virtualView -> virtualView.showStringMessage(content));
    }

    /**
     * @param excludedNick Nickname of the excluded user.
     * @return List of {@link VirtualView} associated with users in the game, except the specified user.
     */
    public List<VirtualView> viewsExcept(String excludedNick) {
        return nickVirtualViewMap.keySet().stream()
                .filter(nick -> !nick.equals(excludedNick))
                .map(nickVirtualViewMap::get)
                .collect(Collectors.toList());
    }

    /**
     * @return List of views of the players in the game except current player.
     */
    public List<VirtualView> viewsExceptCurrentPlayer() {
        return viewsExcept(game.getCurrentPlayerNick());
    }

    /**
     * @return List of the VirtualViews of the players in the game.
     */
    public List<VirtualView> getViews() {
        return nickVirtualViewMap.values().stream().toList();
    }

    /**
     * Broadcasts clients views with up-to-date content.
     */
    public void updateViews() {
        getViews().forEach(vv -> vv.update(new ReducedGame(this.game)));
    }

    /**
     * Shows disconnection message to clients.
     */
    public void handleDisconnection(String nicknameDisconnected) {
        nickVirtualViewMap.remove(nicknameDisconnected);
        getViews().forEach(vv -> vv.shutdown(nicknameDisconnected + " has disconnected"));
    }

    /**
     * manages the request of character card, checks number of coins and then update view
     * @param chosenCard
     */
    private void handleCharacterCardRequest(int chosenCard) {
        CharacterCard card = game.getCharacterCard(chosenCard - 1);

        int playerCoins = game.getCurrentPlayerInstance().getCoins();
        int cardCost = card.getCost();
        boolean CCActivated = game.getCurrentPlayerInstance().isCCActivated();
        if(CCActivated){
            getCurrentPlayerView().showStringMessage("You can't use another Character Card during this turn");
            restoreGameFlow();
            return;
        }


        if (playerCoins < cardCost) {
            getCurrentPlayerView().showStringMessage("You need " + (cardCost - playerCoins) + " more coins to activate this card");
            restoreGameFlow();
            return;
        }

        broadcastExceptCurrentPlayer(game.getCurrentPlayerNick() + " is activating Character Card " + chosenCard + "... ");

        game.getCurrentPlayerInstance().setCCActivated(true);

        switch (card.getType()) {
            case ALL_REMOVE_COLOR -> {
                getCurrentPlayerView().askCCAllRemoveColorInput();
            }
            case BLOCK_COLOR_ONCE -> {
                getCurrentPlayerView().askCCBlockColorOnceInput();
            }
            case BLOCK_TOWER -> {
                BlockTower blockTower = (BlockTower) card;
                blockTower.doEffect();
            }
            case CHOOSE_1_DINING_ROOM -> {
                Choose1DiningRoom choose1DiningRoom = (Choose1DiningRoom) card;
                getCurrentPlayerView().askCCChoose1DiningRoomInput(choose1DiningRoom.allowedColors());
            }
            case CHOOSE_1_TO_ISLAND -> {
                Choose1ToIsland choose1ToIsland = (Choose1ToIsland) card;
                getCurrentPlayerView().askCCChoose1ToIslandInput(card.allowedColors(), game.getIslands().size() + 1);
            }
            case CHOOSE_3_TO_ENTRANCE -> {
                getCurrentPlayerView().askCCChoose3ToEntranceInput(card.allowedColors(), game.getCurrentPlayerInstance().getSchoolDashboard().entranceAsList(), 1);
            }
            case CHOOSE_ISLAND -> {
                getCurrentPlayerView().askCCChooseIslandInput(game.getIslands().size()  + 1);
            }
            case EXCHANGE_2_STUDENTS -> {
                List<Color> entrance = game.getCurrentPlayerInstance().getSchoolDashboard().entranceAsList();
                List<Color> dr = game.getCurrentPlayerInstance().getSchoolDashboard().diningRoomAsList();
                getCurrentPlayerView().askCCExchange2StudentsInput(entrance, dr, 1);
            }
            case NO_ENTRY_ISLAND -> {
                getCurrentPlayerView().askCCNoEntryIslandInput(game.getIslands().size());
            }
            case PLUS_2_INFLUENCE -> {
                Plus2Influence plus2Influence = (Plus2Influence) card;
                plus2Influence.doEffect();

                restoreGameFlow();
            }
            case TEMP_CONTROL_PROF -> {
                TempControlProf tempControlProf = (TempControlProf) card;
                tempControlProf.doEffect();

                restoreGameFlow();
            }
            case TWO_ADDITIONAL_MOVES -> {
                TwoAdditionalMoves twoAdditionalMoves = (TwoAdditionalMoves) card;
                twoAdditionalMoves.doEffect();

                restoreGameFlow();
            }
        }
    }

    /**
     * Calls the character card effect and then restore game flow
     * @param color color
     */
    private void playCCAllRemoveColor(Color color) {
        if(color != null) {
            AllRemoveColor card = (AllRemoveColor) game.getCharacterCard(CharacterCardType.ALL_REMOVE_COLOR);
            card.doEffect(color);
        } else {
            getCurrentPlayerView().askCCAllRemoveColorInput();
        }

        restoreGameFlow();
    }

    /**
     * Calls the character card effect and then restore game flow
     * @param color color
     */
    private void playCCBlockColorOnce(Color color) {
        BlockColorOnce card = (BlockColorOnce) game.getCharacterCard(CharacterCardType.BLOCK_COLOR_ONCE);
        card.doEffect(color);

        restoreGameFlow();
    }
    /**
     * Calls the character card effect and then restore game flow
     * @param color color
     */
    private void playCCChoose1DiningRoom(Color color) {
        Choose1DiningRoom card = (Choose1DiningRoom) game.getCharacterCard(CharacterCardType.CHOOSE_1_DINING_ROOM);
        card.doEffect(color);

        restoreGameFlow();
    }
    /**
     * Calls the character card effect and then restore game flow
     * @param color color
     * @param island number of island
     */
    private void playCCChoose1ToIsland(Color color, int island) {
        Choose1ToIsland choose1ToIsland = (Choose1ToIsland) game.getCharacterCard(CharacterCardType.CHOOSE_1_TO_ISLAND);
        choose1ToIsland.doEffect(color, island);

        restoreGameFlow();
    }
    /**
     * Calls the character card partial effect with a single color from card and from entrance
     * @param fromCard the color chosen from card
     * @param fromEntrance the color chosen from entrance
     * @param inputCount number of chooses (MAX 3)
     */

    private void playCCChoose3ToEntrancePartialSwap(Color fromCard, Color fromEntrance, int inputCount) {
        Choose3toEntrance card = (Choose3toEntrance) game.getCharacterCard(CharacterCardType.CHOOSE_3_TO_ENTRANCE);

        if(inputCount==1)
        game.getCurrentPlayerInstance().removeCoins(card.getCost());

        card.doPartialEffect(fromCard, fromEntrance);

        if (inputCount < 3) {
            inputCount++;

            updateViews();
            getCurrentPlayerView().askCCChoose3ToEntranceInput(card.allowedColors(), game.getCurrentPlayerInstance().getSchoolDashboard().entranceAsList(), inputCount);
        } else {
            restoreGameFlow();
        }
    }

    /**
     *
     * Calls the character card effect with all color from card and from entrance
     * @param fromCard the color chosen from card
     * @param fromEntrance the color chosen from entrance
     *
     */

    private void playCCChoose3ToEntrance(EnumMap<Color, Integer> fromCard, EnumMap<Color, Integer> fromEntrance) {
        Choose3toEntrance choose3toEntrance = (Choose3toEntrance) game.getCharacterCard(CharacterCardType.CHOOSE_3_TO_ENTRANCE);
        choose3toEntrance.doEffect(fromCard, fromEntrance);

        restoreGameFlow();
    }

    /**
     * Calls the character card effect
     * @param chosenIsland number of island
     */
    private void playCCChooseIsland(int chosenIsland) {
        ChooseIsland card = (ChooseIsland) game.getCharacterCard(CharacterCardType.CHOOSE_ISLAND);
        card.doEffect(chosenIsland);

        restoreGameFlow();
    }

    /**
     * Calls the character card effect
     * @param fromEntrance
     * @param fromDiningRoom
     */
    private void playCCExchange2Students(EnumMap<Color, Integer> fromEntrance, EnumMap<Color, Integer> fromDiningRoom) {
        Exchange2Students card = (Exchange2Students) game.getCharacterCard(CharacterCardType.EXCHANGE_2_STUDENTS);
        card.doEffect(fromEntrance, fromDiningRoom);

        restoreGameFlow();
    }

    /**
     * Calls the character card partial effect with a single color from entrance and from dining room
     * @param fromEntrance color from entrance
     * @param fromDiningRoom color from dining room
     * @param inputCount number of iteration (MAX 2)
     */
    private void playCCExchange2StudentsPartialSwap(Color fromEntrance, Color fromDiningRoom, int inputCount) {
        Exchange2Students card = (Exchange2Students) game.getCharacterCard(CharacterCardType.EXCHANGE_2_STUDENTS);

        if(inputCount==1)
            game.getCurrentPlayerInstance().removeCoins(card.getCost());

        card.doPartialEffect(fromEntrance, fromDiningRoom);

        if (inputCount < 2) {
            inputCount++;

            updateViews();
            List<Color> entrance = game.getCurrentPlayerInstance().getSchoolDashboard().entranceAsList();
            List<Color> dr = game.getCurrentPlayerInstance().getSchoolDashboard().diningRoomAsList();
            getCurrentPlayerView().askCCExchange2StudentsInput(entrance, dr, inputCount);
        } else {
            restoreGameFlow();
        }
    }

    /**
     * Calls the character card effect
     * @param island number of island
     */

    private void playCCNoEntryIsland(int island) {
        NoEntryIsland card = (NoEntryIsland) game.getCharacterCard(CharacterCardType.NO_ENTRY_ISLAND);
        card.doEffect(island);

        restoreGameFlow();
    }

    /**
     *When a condition deviates the correct game flow (for example not enough coins, wrong color...)
     */
    private void restoreGameFlow() {
        updateViews();

        if(state instanceof Action1State) {
            askActionPhase1();
        } else if(state instanceof Action2State) {
            askActionPhase2();
        } else if(state instanceof Action3State) {
            askActionPhase3();
        }
    }

    /**
     * Calls the method to process action phase 1 and updates view
     */
    public void askActionPhase1() {
        if(state instanceof Action1State) {
            int action1Moves = ((Action1State) state).getMovesCount();

            if (action1Moves <= 4) {
                getCurrentPlayerView().askActionPhase1(action1Moves, game.getIslands().size(), getGame().isExpertMode());
                broadcastExceptCurrentPlayer(game.getCurrentPlayerNick() + " is playing (action phase 1, move " + action1Moves + ")...");
            }
        }
    }
    /**
     * Calls the method to process action phase 2 and updates view
     */
    public void askActionPhase2() {
        getCurrentPlayerView().askActionPhase2(game.getCurrentPlayerInstance().getMaxSteps(), game.isExpertMode());
        broadcastExceptCurrentPlayer(game.getCurrentPlayerNick() + " is playing (action phase 2)...");
    }
    /**
     * Calls the method to process action phase 3 and updates view
     */
    public void askActionPhase3() {
        getCurrentPlayerView().askActionPhase3(game.getPlayableCloudCards().stream().map(i -> i+1).toList(), game.isExpertMode());
        broadcastExceptCurrentPlayer(game.getCurrentPlayerNick() + " is playing (action phase 3)...");
    }

    /**
     * When there is a winner in the game broadcast all players
     * @param winner
     */
    public void notifyWinner(Player winner) {
        String winnerNick = winner.getNickname();
        System.out.println(gameLogHeader() + " finished: " + winnerNick + " won.");

        viewsExcept(winnerNick).forEach(v -> v.showWinnerToOthers(winnerNick));
        getVirtualView(winnerNick).notifyWinner();
    }

}