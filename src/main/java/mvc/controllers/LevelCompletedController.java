package mvc.controllers;

import mvc.util.FXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import start.Main;

/**
 * Controller for "Level Completed!" dialog.
 *
 */
public class LevelCompletedController extends FXController {
    /** Link to label that shows how many time you used to pass level. */
    @FXML
    Label passingTimeLabel;
    /** Link to label that shows how many moves you used to pass level. */
    @FXML
    Label moveCountLabel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // If you close window you'll be moved into main menu
        passingTimeLabel.getScene().getWindow().setOnCloseRequest(event ->
                Main.changeScene("main.fxml"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

    }

    /**
     * Event handler for all buttons.
     *
     * @param event action event from button.
     */
    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Closing window of dialog. It doesn't matter to know what button was pressed before this action
        ((Stage) clickedButton.getScene().getWindow()).hide();

        switch (clickedButton.getId()) {
            case "mainMenuButton":
                Main.changeScene("main.fxml");
                break;

            case "nextLevelButton":
                Main.changeScene("main.fxml");
                break;
        }
    }
}
