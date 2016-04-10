package control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import start.Main;

import java.util.Optional;

/**
 * Controller for main menu.
 *
 */
public class MainMenuController extends FXController {
    @FXML
    private Label versionLabel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        versionLabel.setText(Main.getProductVersion());
//        parseChildren(exitDialog.getDialogPane(), 0);
    }

//    private String cloneChar(char c, int times) {
//        String out = "";
//
//        for (int i = 0; i < times; i++) {
//            out += c;
//        }
//
//        return out;
//    }
//
//    private void parseChildren(Node node, int multiplicity) {
//        if (node instanceof Parent && !(node instanceof Pane) && !(node instanceof ButtonBar)) {
//            System.out.println(cloneChar('\t', multiplicity) + node);
//            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
//                parseChildren(child, multiplicity + 1);
//            }
//        } else if (node instanceof Pane) {
//            System.out.println(cloneChar('\t', multiplicity) + node);
//            for (Node child : ((Pane) node).getChildren()) {
//                parseChildren(child, multiplicity + 1);
//            }
//        } else if (node instanceof ButtonBar) {
//            System.out.println(cloneChar('\t', multiplicity) + node);
//            for (Node child : ((ButtonBar) node).getButtons()) {
//                parseChildren(child, multiplicity + 1);
//            }
//        } else {
//            System.out.println(cloneChar('\t', multiplicity) + node);
//        }
//    }

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
     * Event handler for all menu buttons.
     *
     * @param event event from button.
     */
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
                Optional result = Main.getExitDialog().showAndWait();

                if (result.isPresent() && result.get() == Main.EXIT_OPTION) {
                    System.exit(0);
                } else if (result.isPresent() && result.get() == Main.CANCEL_OPTION) {
                    Main.getExitDialog().hide();
                }

                break;
        }
    }
}
