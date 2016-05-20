package mvc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import mvc.help.FXController;
import start.Main;

import java.util.Optional;

/**
 * Controller for main menu.
 */
public class MainMenuController extends FXController {
    @FXML
    private Label versionLabel;

    private Alert exitDialog;
    public Alert getExitDialog() {
        return exitDialog;
    }
    private ButtonType exitOption;
    public ButtonType getExitOption() {
        return exitOption;
    }
    private ButtonType cancelOption;
    public ButtonType getCancelOption() {
        return cancelOption;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        versionLabel.setText(Main.getProductVersion());
        setupExitDialog();
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

    private void setupExitDialog() {
        exitOption = new ButtonType(getLocaleStr("exit"), ButtonBar.ButtonData.YES);
        cancelOption = new ButtonType(getLocaleStr("cancel"), ButtonBar.ButtonData.NO);

        exitDialog = new Alert(Alert.AlertType.CONFIRMATION, getLocaleStr("dialogs.body.exit-from-game"),
                exitOption, cancelOption);
        exitDialog.setTitle(getLocaleStr("header.base") + " - " + getLocaleStr("exit"));
        exitDialog.setHeaderText(getLocaleStr("dialogs.head.exit-from-game"));

        // Adding CSS-Stylesheet to customize dialog, for example, fonts
        exitDialog.getDialogPane().getStylesheets().add(Main.getResourceURL("styles/bigger-dialog-fonts.css").toExternalForm());
        exitDialog.getDialogPane().getStyleClass().add("dialog-body");

        // And, customizing dialog with setters
        Label contentLabel = (Label) exitDialog.getDialogPane().lookup(".content");
        contentLabel.setWrapText(true);

        exitDialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        exitDialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        exitDialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);
    }

    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        switch (clickedButton.getId()) {
            case "playButton":
                Main.changeScene("campaign-select.fxml", " - " + getLocaleStr("campaign.mode.select.header"));
                break;

            case "createButton":
                break;

            case "settingsButton":
                Main.changeScene("settings-menu.fxml", " - " + getLocaleStr("settings"));
                break;

            case "exitButton":
                // Class "Main" stores this exit dialog
                Optional result = exitDialog.showAndWait();

                if (result.isPresent() && result.get() == exitOption) {
                    System.exit(0);
                } else if (result.isPresent() && result.get() == cancelOption) {
                    exitDialog.hide();
                }

                break;
        }
    }
}
