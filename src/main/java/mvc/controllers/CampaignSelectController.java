package mvc.controllers;

import mvc.util.FXController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import start.Main;

/**
 * Controller for selecting campaign.
 *
 */
public class CampaignSelectController extends FXController {
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
     * @param event event from button.
     */
    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        switch (clickedButton.getId()) {
            case "backButton":
                Main.changeScene("main.fxml");
                break;

            case "classicButton":
                Main.changeScene("level-select.fxml", " - " + getLocaleStr("campaign.mode.classic") + ", "
                        + getLocaleStr("levels.select.header"),
                        fxController -> {
                            ((LevelSelectController) fxController).levelsTable.setItems(
                                    FXCollections.observableList(Main.CLASSIC_CAMPAIGN));
                            return fxController;
                        });
                break;

            case "extendedButton":
                Main.changeScene("level-select.fxml", " - " + getLocaleStr("campaign.mode.extended") + ", "
                        + getLocaleStr("levels.select.header"),
                        fxController -> {
                            ((LevelSelectController) fxController).levelsTable.setItems(
                                    FXCollections.observableList(Main.EXTENDED_CAMPAIGN));
                            return fxController;
                        });
                break;
        }
    }
}
