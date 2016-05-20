package mvc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import levels.Level;
import levels.LevelPack;
import mvc.help.ExternalStorage;
import mvc.help.FXController;
import start.Main;
import util.future.FutureTasks;

import java.util.Optional;

/**
 * Controller for "Level Completed!" dialog.
 */
public class LevelCompletedController extends FXController {
    @FXML
    public Label passingTimeLabel;
    @FXML
    public Label moveCountLabel;
    private Alert campaignCompletedDialog;

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
        passingTimeLabel.getScene().getWindow().setOnCloseRequest(event ->
                Main.changeScene("main.fxml"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

    }

    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // It doesn't matter what button was pressed before closing dialog
        clickedButton.getScene().getWindow().hide();

        final LevelPack selectedCampaign = ExternalStorage.getInstance().selectedCampaign;
        final Level currentLevel = ExternalStorage.getInstance().currentLevel;
        final Optional<Level> nextLevel = selectedCampaign.getLevelAfter(currentLevel);

        switch (clickedButton.getId()) {
            case "mainMenuButton":
                if (nextLevel.isPresent())
                    Main.changeScene("main.fxml");
                else
                    showCampaignCompletedDialog(() ->
                            Main.changeScene("main.fxml"));
                break;

            case "nextLevelButton":
                if (nextLevel.isPresent()) {
                    ExternalStorage.getInstance().currentLevel = nextLevel.get();
                    Main.changeScene("game-field.fxml", " - " + getLocaleStr("header.game-play"));
                } else
                    showCampaignCompletedDialog(() ->
                            Main.changeScene("campaign-select.fxml",
                                    " - " + getLocaleStr("campaign.mode.select.header")));


                break;
        }
    }

    private void showCampaignCompletedDialog(Runnable nextTask) {
        // Run later with permissions to show properly new stage
        FutureTasks.runLaterWithPermissions(() -> {
            if (campaignCompletedDialog == null)
                setupCampaignCompletedDialog();
            campaignCompletedDialog.showAndWait();
            nextTask.run();
        });
    }

    private void setupCampaignCompletedDialog() {
        ButtonType okOpt = new ButtonType("OK!", ButtonBar.ButtonData.OK_DONE);

        campaignCompletedDialog = new Alert(Alert.AlertType.INFORMATION,
                String.format(getLocaleStr("campaign.completed.fmt-str"),
                        ExternalStorage.getInstance().selectedCampaign.getName()),
                okOpt);
        campaignCompletedDialog.setTitle(getLocaleStr("header.base") + " - " +
                getLocaleStr("campaign.completed.header"));
        campaignCompletedDialog.setHeaderText(getLocaleStr("congratulations"));

        // Adding CSS-Stylesheet to customize dialog's fonts
        campaignCompletedDialog.getDialogPane().getStylesheets().add(
                Main.getResourceURL("styles/bigger-dialog-fonts.css").toExternalForm());
        campaignCompletedDialog.getDialogPane().getStyleClass().add("dialog-body");

        campaignCompletedDialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        campaignCompletedDialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        campaignCompletedDialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);
    }
}
