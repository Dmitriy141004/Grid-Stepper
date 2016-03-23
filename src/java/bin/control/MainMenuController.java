package bin.control;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import bin.start.Main;

import java.util.Optional;

/**
 * Controller for main menu.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 *
 */
public class MainMenuController extends FXController {

    @Override
    public void init() {
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
     * Event handler for all menu buttons. Moved in one method because of {@link ActionEvent} as param - it was not used.
     *
     * @param event event from button.
     */
    public void actionButtonPressed(ActionEvent event) {
        Object source = event.getSource();

        // if source isn't button - we don't need to do anything else
        if (!(source instanceof Button)) {
            return;
        }

        Button clickedButton = (Button) source;

        switch (clickedButton.getId()) {
            case "playButton":
                Main.changeScene("game-field.fxml", getLocaleStr("header.base") + " - " + getLocaleStr("header.game-play"));
                break;

            case "createButton":
                break;

            case "settingsButton":
                Main.changeScene("settings-menu.fxml", getLocaleStr("header.base") + getLocaleStr("settings"));
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
