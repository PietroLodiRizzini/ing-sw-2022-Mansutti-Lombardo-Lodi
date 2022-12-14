package it.polimi.ingsw;

import it.polimi.ingsw.controller.ClientController;
import it.polimi.ingsw.view.cli.Cli;
import it.polimi.ingsw.view.gui.Gui;
import it.polimi.ingsw.view.gui.GuiManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Client's main method 
 */
public class Client {
    public static void main(String[] args) {
        String mode = "";

        for(int i = 0; i < args.length; i++)
            if((args[i].equals("--view") || args[i].equals("-v")) && args.length > i + 1) {
                mode = args[i+1].toUpperCase();
                break;
            }

        if (! (mode.equals("CLI") || mode.equals("GUI")))
            mode = Cli.askViewModeAtStart();

        if (mode.equals("CLI")) {
            Cli cli = new Cli();
            ClientController cc = new ClientController(cli);
            cli.addObserver(cc);
            cli.begin();
        } else {
            new Thread(Gui::main).start();
            GuiManager gui = new GuiManager();
            ClientController cc = new ClientController(gui);
            gui.addObserver(cc);
        }
    }
}
