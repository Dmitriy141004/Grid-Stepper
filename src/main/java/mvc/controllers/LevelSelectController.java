package mvc.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import levels.Level;
import levels.LevelPack;
import mvc.util.ExternalStorage;
import mvc.util.FXController;
import start.Main;

import java.util.stream.Collectors;

/**
 * Controller for level select menu.
 */
public class LevelSelectController extends FXController {
    @FXML
    private BorderPane levelsPaneWrapper;

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
        final LevelPack selectedCampaign = ExternalStorage.getInstance().selectedCampaign;

        if (!selectedCampaign.isEmpty()) {
            FlowPane levelsPane = new FlowPane();
            levelsPane.getChildren().addAll(selectedCampaign.stream()
                    // Transforming Level objects to buttons
                    .map(Level::getButtonRepresentation)
                    .collect(Collectors.toList()));

            levelsPane.setPadding(new Insets(Level.LEVEL_BUTTON_MARGIN));
            levelsPaneWrapper.setCenter(levelsPane);

        } else {
            Label sorryLabel = new Label(getLocaleStr("campaign.no-levels-label"));
            sorryLabel.setFont(new Font(Main.APP_FONT_NAME, 18));
            levelsPaneWrapper.setCenter(sorryLabel);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

    }

    public void backAction() {
        Main.changeScene("campaign-select.fxml", " - " + getLocaleStr("campaign.mode.select.header"));
    }
}
