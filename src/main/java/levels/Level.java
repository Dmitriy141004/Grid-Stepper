package levels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import levels.cells.LevelCell;
import mvc.help.ExternalStorage;
import start.Main;

import java.util.ArrayList;

/**
 * Class for storing data about <b>one</b> level.
 *
 * @see #number
 * @see #completed
 * @see #grid
 */
public class Level implements Cloneable {
    public static final int LEVEL_BUTTON_MARGIN = 20;
    private static final int LEVEL_COMPLETION_BAR_SPACING = 2;
    private static final int LEVEL_BUTTON_SIZE = 60;
    /** Level's number. XSD schema for level packs says, that level number's min length is {@code 1}, and max length is
     * {@code 5}. */
    private String number;
    public String getNumber() {
        return number;
    }

    private volatile boolean completed;
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        buttonRepresentation.setDisable(!completed);
        levelCompletionBar.setProgress(1);
    }


    private ArrayList<ArrayList<LevelCell>> grid;
    public ArrayList<ArrayList<LevelCell>> getGrid() {
        return grid;
    }
    /** Button representation of level, is used when selecting level. */
    private Button buttonRepresentation;
    public Button getButtonRepresentation() {
        return buttonRepresentation;
    }

    private ProgressBar levelCompletionBar;

    public Level(String number, ArrayList<ArrayList<levels.cells.LevelCell>> grid, boolean completed) {
        this.number = number;
        this.completed = completed;
        this.grid = grid;
        this.buttonRepresentation = makeButtonRepresentation();
    }

    private Button makeButtonRepresentation() {
        Button buttonRepresentation = makeLevelButton();
        if (!completed)
            buttonRepresentation.setDisable(true);

        buttonRepresentation.setOnMouseClicked(event -> {
            ExternalStorage.getInstance().currentLevel = this;
            Main.changeScene("game-field.fxml", " - " + Main.getLocaleStr("header.game-play"));
        });

        return buttonRepresentation;
    }

    private Button makeLevelButton() {
        Label levelNumber = makeNumberLabel();
        // If level is completed - this bar will be filled on 100%, otherwise - on 0%
        levelCompletionBar = makeLevelCompletionBar();

        Button buttonRepresentation = new Button("", new BorderPane(levelNumber, null, null, levelCompletionBar, null));

        buttonRepresentation.setPadding(new Insets(0, 0, LEVEL_COMPLETION_BAR_SPACING, 0));
        buttonRepresentation.setPrefSize(LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE);
        buttonRepresentation.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        FlowPane.setMargin(buttonRepresentation, new Insets(0, LEVEL_BUTTON_MARGIN, LEVEL_BUTTON_MARGIN, 0));

        return buttonRepresentation;
    }

    private Label makeNumberLabel() {
        Label levelNumber = new Label(number);

        levelNumber.setFont(new Font(Main.APP_FONT_NAME, 15));
        BorderPane.setAlignment(levelNumber, Pos.CENTER);

        return levelNumber;
    }

    private ProgressBar makeLevelCompletionBar() {
        ProgressBar levelCompletionBar = new ProgressBar();

        levelCompletionBar.setProgress(completed ? 1 : 0);
        BorderPane.setAlignment(levelCompletionBar, Pos.BOTTOM_CENTER);
        levelCompletionBar.setPrefWidth(LEVEL_BUTTON_SIZE - LEVEL_COMPLETION_BAR_SPACING * 2);
        levelCompletionBar.setMouseTransparent(true);

        return levelCompletionBar;
    }
}
