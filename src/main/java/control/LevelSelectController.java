package control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import levels.Level;
import start.Main;

/**
 * Controller for level select menu.
 *
 * <p><span style="color: #A8C023; font-style: italic">
 *     TODO: Upgrade level select menu (from simple table to "Angry Birds"-like menu).
 * </span></p>
 *
 */
public class LevelSelectController extends FXController {
    /** Link to table with levels. All columns are got by {@code fx:id} using {@code switch}-construction, not
     * class fields with {@link FXML @FXML}. */
    @FXML
    TableView<Level> levelsTable;
    @FXML
    private Button playButton;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        // Setting value factories to columns of table
        for (TableColumn<Level, ?> column : levelsTable.getColumns()) {
            switch (column.getId()) {
                case "levelNumbersColumn":
                    column.setCellValueFactory(new PropertyValueFactory<>("number"));
                    break;

                case "levelNamesColumn":
                    column.setCellValueFactory(new PropertyValueFactory<>("name"));
                    break;

                case "levelCompletionColumn":
                    column.setCellValueFactory(new PropertyValueFactory<>("completed"));
            }
        }

        // Adding two-click-listener to table
        levelsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                Main.changeScene("game-field.fxml", " - " + getLocaleStr("header.game-play"),
                    fxController -> {
                        ExternalStorage.getInstance().currentLevel = levelsTable.getSelectionModel().getSelectedItem();
                        return fxController;
                    });
        });

        // If there's no selection in table, "Play" button must be disabled (and it is disabled by default)
        levelsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
            playButton.setDisable(newValue == null));
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
                Main.changeScene("campaign-select.fxml");
                break;

            case "playButton":
                Main.changeScene("game-field.fxml", " - " + getLocaleStr("header.game-play"),
                        fxController -> {
                            ExternalStorage.getInstance().currentLevel = levelsTable.getSelectionModel().getSelectedItem();
                            return fxController;
                        });
        }
    }
}
