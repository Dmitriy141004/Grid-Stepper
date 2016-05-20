package mvc.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import mvc.help.ExternalStorage;
import mvc.help.FXController;
import start.Main;

/**
 * Controller for selecting campaign.
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

    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        switch (clickedButton.getId()) {
            case "backButton":
                Main.changeScene("main.fxml");
                break;

            case "classicButton":
                ExternalStorage.getInstance().selectedCampaign = Main.classicCampaign;
                Main.changeScene("level-select.fxml", " - " + getLocaleStr("campaign.mode.classic") + ", "
                        + getLocaleStr("levels.select.header"));
                break;

            case "extendedButton":
                ExternalStorage.getInstance().selectedCampaign = Main.extendedCampaign;
                Main.changeScene("level-select.fxml", " - " + getLocaleStr("campaign.mode.extended") + ", "
                        + getLocaleStr("levels.select.header"));
                break;
        }
    }
}
