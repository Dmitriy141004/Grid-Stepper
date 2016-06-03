package mvc.controllers.gameplay;

import com.sun.javafx.geom.Line2D;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import levels.Level;
import levels.cells.CellType;
import levels.cells.LevelCell;
import levels.cells.StartCell;
import mvc.controllers.LevelCompletedController;
import mvc.help.ExternalStorage;
import mvc.help.FXController;
import start.Main;
import util.collections.LIFOQueue;
import util.collections.Stack;
import util.javafx.animation.ExtendedAnimationTimer;
import util.javafx.scenes.SceneContent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Main game play controller. It's the most difficult. Here're all game logic.
 */
public class GamePlayController extends FXController {
    @FXML
    private volatile Canvas gameCanvas;
    private volatile GraphicsContext graphics;
    /** {@link HBox} with "end game", "redo", "undo" and "restart" buttons. Used to reset focus from buttons when moving
     * GP to make arrows normally work to move GP. */
    @FXML
    private HBox ctrlButtonsBox;

    public static final int CELL_SIZE = 45;

    /** This pointer points, on what <b>X coordinate</b> is it now. <i><b>Note:</b> it's a co-ord on canvas, not on level.
     * </i> */
    private volatile int pointerX;
    /** This pointer points, on what <b>Y coordinate</b> is it now. <i><b>Note:</b> it's a co-ord on canvas, not on level.
     * </i> */
    private volatile int pointerY;
    private static final int GAME_POINTER_SIZE = 25;
    private static final Color GAME_POINTER_COLOR =
            Color.web(Main.getAppSettings().getSettingOrElse("gp-color", "#439D1C"));
    private volatile boolean movingPointer = false;
    /** Handler for key input. Extracted to object because I need to remove it from list of handlers when
     * {@link #actionButtonPressed(ActionEvent)} receives action from "end game" button. */
    private volatile EventHandler<KeyEvent> GP_MOVE_EVENT_HANDLER;

    private volatile int levelGridWidth;
    private volatile int levelGridHeight;

    /**
     * Undo stack for moves. When you press {@code Ctrl+Z} or use button "Undo" - last move from this stack moves to
     * {@link #redoStack}, and it's also being executed. This stack appends when you move.
     *
     * @see #doUndo()
     * @see #doRedo()
     *
     */
    private volatile LIFOQueue<Move> undoStack = new Stack<>();
    private volatile boolean undoFlag = false;
    /**
     * Redo stack for moves. When you press {@code Ctrl+R} or use button "Redo" - last move from this stack moves to
     * {@link #undoStack}, and it's also being executed.
     *
     * @see #doUndo()
     * @see #doRedo()
     *
     */
    private volatile LIFOQueue<Move> redoStack = new Stack<>();

    private volatile Alert exitDialog;
    private static final ButtonType EXIT_OPTION = new ButtonType(Main.getLocaleStr("exit"), ButtonBar.ButtonData.YES);
    private static final ButtonType CANCEL_OPTION = new ButtonType(Main.getLocaleStr("cancel"), ButtonBar.ButtonData.NO);

    private volatile List<Line2D> stepLines = new ArrayList<>(0);

    public volatile int startCellX;
    public volatile int startCellY;
    private volatile StartCell startCell;

    private volatile double startTime = System.nanoTime();

    private class PointerMoveAnimation extends ExtendedAnimationTimer {
        /** End position of pointer move on X axis. By default it equals {@link #pointerX}, but in
         * {@link #PointerMoveAnimation(Move, StepCause)}  constructor} it's changed by move value. */
        int goalX = pointerX;
        /** End position of pointer move on Y axis. By default it equals {@link #pointerY}, but in
         * {@link #PointerMoveAnimation(Move, StepCause) constructor} it's changed by move value. */
        int goalY = pointerY;
        /** Speed ({@code pixels/frame}) of pointer movement. It can be only a multiple of {@code 3} or {@code 5}, and also
         * it can equal {@code 1}. */
        final int MOVE_SPEED = 3;

        /**
         * Variable-stopper for method {@link #start()}.
         *
         * @see #stop()
         * @see #start()
         *
         */
        boolean stopped = false;

        /** Saved {@code x} position to draw line when moving GP (line start = this var, line end = {@link #pointerX}). */
        int startX = pointerX;
        /** Saved {@code y} position to draw line when moving GP (line start = this var, line end = {@link #pointerY}). */
        int startY = pointerY;

        PointerMoveAnimation(Move moveToDo, StepCause stepCause) {
            movingPointer = true;
            setupGoalCoordinates(moveToDo);
            if (canStepOnGoalCell()) {
                // Thinking, what to do with stacks (if reached this place, step will be done)
                switch (stepCause) {
                    case KEY_WAS_PRESSED:
                        undoStack.push(moveToDo);
                        redoStack.clear();
                        break;

                    case DOING_REDO:
                    case DOING_UNDO:
                        undoStack.pop();
                        redoStack.push(moveToDo);
                        break;
                }
            } else
                stop();
        }

        private boolean canStepOnGoalCell() {
            LevelCell goalCell = currentLevel().getGrid().get(goalX / CELL_SIZE).get(goalY / CELL_SIZE);
            if (goalCell.getType() == CellType.WALL || goalCell.getType() == CellType.BACKGROUND_SQUARE ||
                    (goalCell.isVisited()) && !undoFlag) {
                stop();
                return false;
            }
            return true;
        }

        private void setupGoalCoordinates(Move move) {
            switch (move) {
                case UP:
                    if (pointerY == 0) {
                        stop();
                        return;
                    }

                    goalY = pointerY - CELL_SIZE;
                    break;

                case LEFT:
                    if (pointerX == 0) {
                        stop();
                        return;
                    }

                    goalX = pointerX - CELL_SIZE;
                    break;

                case DOWN:
                    if (pointerY >= (levelGridHeight - 1) * CELL_SIZE) {
                        stop();
                        return;
                    }

                    goalY = pointerY + CELL_SIZE;
                    break;

                case RIGHT:
                    if (pointerX >= (levelGridWidth - 1) * CELL_SIZE) {
                        stop();
                        return;
                    }

                    goalX = pointerX + CELL_SIZE;
                    break;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handle(long now) {
            incrementCoordinates();
            redrawField();
            drawStepLines();
            startCell.draw(startCellX, startCellY, graphics, GamePlayController.this);
            drawGP();

            // If we reached goal coordinates...
            if (pointerX == goalX && pointerY == goalY) {
                // If undo flag is null, we add new line to stepLines
                if (!undoFlag) stepLines.add(new Line2D(startX, startY, pointerX, pointerY));
                currentLevel().getGrid().get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).setVisited(true);

                LevelCell current = currentLevel().getGrid().get(goalX / CELL_SIZE).get(goalY / CELL_SIZE);
                if (current.getType() == CellType.FINISH && areAllCellsVisited()) {
                    // This thing will be activated after timer stop
                    // I use this because if AnimationTimer isn't stopped, you can't show new stage
                    // And my own implementation allows to do this
                    setEndAction(() -> {
                        removeGPMovement();

                        // Getting index of current level
                        int currentLvlI = ExternalStorage.getInstance().selectedCampaign.indexOfLevel(currentLevel());
                        currentLevel().setCompleted(true);

                        if (currentLvlI + 1 < ExternalStorage.getInstance().selectedCampaign.levelsCount())
                            ExternalStorage.getInstance().selectedCampaign.getLevel(currentLvlI + 1)
                                    .getButtonRepresentation().setDisable(false);

                        // Small delay
                        delay(500);
                        // Getting scene content
                        SceneContent sceneContent = Main.getSceneContent("level-completed-dialog.fxml");

                        // Creating and customising window/stage
                        Stage levelCompletedDialog = new Stage();
                        levelCompletedDialog.setTitle(getLocaleStr("header.base") + " - " +
                                getLocaleStr("levels.completed.header"));
                        levelCompletedDialog.setMinHeight(150);
                        levelCompletedDialog.setMinWidth(300);
                        levelCompletedDialog.setResizable(false);
                        levelCompletedDialog.setScene(sceneContent.scene);
                        levelCompletedDialog.initModality(Modality.WINDOW_MODAL);
                        levelCompletedDialog.initOwner(Main.primaryStage);

                        // Initializing controller
                        LevelCompletedController controller = (LevelCompletedController) sceneContent.controller;
                        controller.shutdown();
                        controller.wakeUp();
                        // Setting count of moves and time used to complete level
                        controller.moveCountLabel.setText(String.valueOf(stepLines.size()));
                        controller.passingTimeLabel.setText((new BigDecimal(
                                // Converting passing time from nano seconds to seconds
                                // 1. 1 microsecond = 1000 nanoseconds
                                // 2. 1 millisecond = 1000 microseconds
                                // 3. 1 second      = 1000 milliseconds
                                ((double) System.nanoTime() - startTime) / 1_000_000_000.0)
                                // Rounding received passing time in seconds to third digit after point
                                .round(new MathContext(4))) + " " + getLocaleStr("abbreviations.seconds"));

                        // Showing stage/window
                        levelCompletedDialog.showAndWait();
                    });
                }

                stop();
            }
        }

        private boolean areAllCellsVisited() {
            boolean allCellsAreVisited = true;

            for (ArrayList<LevelCell> levelPart : currentLevel().getGrid()) {
                for (LevelCell levelCell : levelPart) {
                    // If cell is neither empty nor finish and it isn't visited -
                    // level isn't completed
                    if (Arrays.asList(CellType.EMPTY,
                            CellType.FINISH).contains(levelCell.getType())
                            && !levelCell.isVisited()) {
                        allCellsAreVisited = false;
                        break;
                    }
                }
            }
            return allCellsAreVisited;
        }

        private void drawGP() {
            graphics.setFill(GAME_POINTER_COLOR);
            graphics.fillOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                    GAME_POINTER_SIZE, GAME_POINTER_SIZE);
            graphics.strokeOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                    GAME_POINTER_SIZE, GAME_POINTER_SIZE);
        }

        private void drawStepLines() {
            graphics.setStroke(Color.BLACK);
            graphics.setLineWidth(4);

            // This draws current step line, GP will be over this line
            if (!undoFlag) {
                // startX, startY -> pointerX, pointerY
                graphics.strokeLine(startX + CELL_SIZE / 2, startY + CELL_SIZE / 2,
                        pointerX + CELL_SIZE / 2, pointerY + CELL_SIZE / 2);
            } else {
                // pointerX, pointerY -> goalX, goalY
                graphics.strokeLine(pointerX + CELL_SIZE / 2, pointerY + CELL_SIZE / 2,
                        goalX + CELL_SIZE / 2, goalY + CELL_SIZE / 2);
            }

            // This draws other step lines
            stepLines.forEach(line -> graphics.strokeLine(line.x1 + CELL_SIZE / 2, line.y1 + CELL_SIZE / 2,
                    line.x2 + CELL_SIZE / 2, line.y2 + CELL_SIZE / 2));

            graphics.setLineWidth(1);
        }

        private void incrementCoordinates() {
            if (pointerX < goalX) pointerX += MOVE_SPEED;
            if (pointerY < goalY) pointerY += MOVE_SPEED;
            if (pointerX > goalX) pointerX -= MOVE_SPEED;
            if (pointerY > goalY) pointerY -= MOVE_SPEED;
        }

        /**
         * Overrides method {@link ExtendedAnimationTimer#stop()} because there're some calls of this method before starting
         * animation. So, uses {@link #stopped variable-stopper}.
         *
         */
        @Override
        public void start() {
            if (!stopped) super.start();
        }

        /**
         * Overrides method {@link ExtendedAnimationTimer#stop()}. Why I have chosen to override? That's because of
         * {@link #movingPointer}. This method is called many times, and I don't wanted to type such code:
         *
         * <pre><code>
         * movingPointer = false;
         * stop();
         * // return;
         * </code></pre>
         *
         */
        @Override
        public void stop() {
            super.stop();
            stopped = true;
            movingPointer = false;
            undoFlag = false;
        }
    }

    /**
     * Safe wrapper for {@link Thread#sleep(long)}.
     *
     * @param howLong delay time in milliseconds
     * @throws RuntimeException if catches {@link InterruptedException}, and sets cause to caught
     *                          {@link InterruptedException}.
     */
    private void delay(long howLong) {
        try {
            Thread.sleep(howLong);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void startMovingPointer(Move moveToDo, StepCause stepCause) {
        // Starting GP move animation
        PointerMoveAnimation pointerMoveAnimation = new PointerMoveAnimation(moveToDo, stepCause);
        pointerMoveAnimation.start();
    }

    /**
     * This enum is used in {@link FieldDrawAnimation}, there're states/"frames"/points of pointer spawn animation.
     *
     */
    private enum PointerSpawnState {
        /**
         * This state is 1st. When it comes - pointer waits for {@code 200 milliseconds} and becomes visible
         * with its normal color.
         *
         */
        DRAW_NORMAL_POINTER(200),
        /**
         * This state is 2nd. When it comes - pointer waits for {@code 200 milliseconds} and sets its color to inverted.
         *
         */
        DRAW_INVERTED_POINTER(200),
        /**
         * This state is 3rd. When it comes - pointer waits for {@code 200 milliseconds} and sets color to normal.
         *
         */
        END_OF_ANIMATION(200);

        /**
         * Length of animation state/"frame"/point in milliseconds.
         *
         */
        private int length;

        /**
         * Getter for {@link #length} field.
         *
         * @return value of {@link #length} field.
         */
        public int getLength() {
            return length;
        }

        /**
         * Creates new animation part with given length
         *
         * @param length length of this state/"frame"/point.
         */
        PointerSpawnState(int length) {
            this.length = length;
        }
    }

    /**
     * Animation of pointer's spawn. Wrapped in class because it's easier to make javadoc for class, not for variable or field.
     * By the way, what this animation does?
     *
     * <ol>
     *     <li>Firstly, there're two loops (for iterating {@code x} and {@code y} points). Idea is to wait time (it's
     *     in {@link FieldDrawAnimation#CELL_DRAW_STEP_DELAY}) between iterating steps. There're any {@code for}
     *     loops in this class, but they're implemented with two variables ({@link #x} and {@link #y}) and {@code if}
     *     statements.</li>
     *
     *     <li>When all cells are drown - class starts doing animation for GP. There're three steps:
     *         <ol>
     *             <li>Drawing GP with normal color;</li>
     *             <li>Drawing GP with inverted color (to make blink effect);</li>
     *             <li>Drawing GP with normal color and ending animation.</li>
     *         </ol>
     *         And, after those all, class setups controls, such as GP control.
     *     </li>
     * </ol>
     *
     */
    private class FieldDrawAnimation extends ExtendedAnimationTimer {
        /**
         * Delay between cell drawings.
         *
         */
        static final int CELL_DRAW_STEP_DELAY = 40;
        /**
         * Current x position.
         *
         */
        int x = 0;
        /**
         * Current y position.
         *
         */
        int y = 0;

        /**
         * State of GP spawn animation. When it's {@code null} - there's no GPSA (<b>G</b>ame <b>P</b>ointer <b>S</b>pawn
         * <b>A</b>nimation), when it isn't {@code null} - animation runs.
         *
         */
        PointerSpawnState animationState = null;

        /**
         * <p>Inverts color. For example, this color:</p>
         *
         * <div style="border: 1px solid black; width: 40px; height: 20px; background-color: #439D1C;"></div>
         *
         * <p>will be turned to this one:</p>
         *
         * <div style="border: 1px solid black; width: 40px; height: 20px; background-color: #BC62E3;"></div>
         *
         * @param source source color
         * @return inverted color.
         */
        Color invertColor(Color source) {
            return Color.rgb(255 - (int) Math.round(source.getRed() * 255.0),
                    255 - (int) Math.round(source.getGreen() * 255.0),
                    255 - (int) Math.round(source.getBlue() * 255.0));
        }

        /**
         * Implementation of {@link ExtendedAnimationTimer#start()}. Here're all animation logic.
         *
         * @param now time in nanoseconds (same as {@link System#nanoTime()}, but with some additions)
         *
         */
        @Override
        public void handle(long now) {
            redrawFieldCell(x, y);

            if (x * CELL_SIZE == startCellX && y * CELL_SIZE == startCellY)
                startCell.draw(startCellX, startCellY, graphics, GamePlayController.this);

            // If all cells are drown, start GPSA
            if (x >= levelGridWidth - 1 && y >= levelGridHeight - 1 && animationState == null) {
                animationState = PointerSpawnState.DRAW_NORMAL_POINTER;
                return;
            }

            // GPSA - 1st step
            if (animationState == PointerSpawnState.DRAW_NORMAL_POINTER) {
                delay(animationState.getLength());

                graphics.setFill(GAME_POINTER_COLOR);
                graphics.fillOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                        pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, GAME_POINTER_SIZE, GAME_POINTER_SIZE);
                graphics.strokeOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                        pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, GAME_POINTER_SIZE, GAME_POINTER_SIZE);

                animationState = PointerSpawnState.DRAW_INVERTED_POINTER;
                return;
            }

            // GPSA - 2nd step
            if (animationState == PointerSpawnState.DRAW_INVERTED_POINTER) {
                delay(animationState.getLength());

                Color invertedPointerColor = invertColor(GAME_POINTER_COLOR);

                graphics.setFill(invertedPointerColor);
                graphics.fillOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                        pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, GAME_POINTER_SIZE, GAME_POINTER_SIZE);
                graphics.strokeOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                        pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, GAME_POINTER_SIZE, GAME_POINTER_SIZE);

                animationState = PointerSpawnState.END_OF_ANIMATION;
                return;
            }

            // GPSA - 3nd step
            if (animationState == PointerSpawnState.END_OF_ANIMATION) {
                delay(animationState.getLength());

                graphics.setFill(GAME_POINTER_COLOR);
                graphics.fillOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                        pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, GAME_POINTER_SIZE, GAME_POINTER_SIZE);
                graphics.strokeOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                        pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, GAME_POINTER_SIZE, GAME_POINTER_SIZE);

                // GPSA - 4th step
                setupGPMovement();
                stop();
                return;
            }

            // This's "for loop"
            y++;
            if (y > levelGridHeight - 1) {
                x++;
                y = 0;
            }

            delay(CELL_DRAW_STEP_DELAY);
        }
    }

    /**
     * Setups {@link KeyEvent} handler for moving GP.
     *
     */
    private void setupGPMovement() {
        GP_MOVE_EVENT_HANDLER = (KeyEvent event) -> {
            if (!movingPointer) {
                ctrlButtonsBox.requestFocus();

                KeyCode keyCode = event.getCode();

                if (keyCode == KeyCode.Z && event.isControlDown()) {
                    // Undo on Ctrl+Z
                    doUndo();
                } else if (keyCode == KeyCode.R && event.isControlDown()) {
                    // Redo on Ctrl+R
                    doRedo();
                } else if (Arrays.asList(KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D,
                        KeyCode.UP, KeyCode.LEFT, KeyCode.DOWN, KeyCode.RIGHT).contains(keyCode))
                    startMovingPointer(Move.getStepFromKeyCode(keyCode), StepCause.KEY_WAS_PRESSED);
            }
        };
        Main.primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
    }

    /**
     * Removes {@link KeyEvent} handler for moving GP.
     *
     */
    private void removeGPMovement() {
        if (GP_MOVE_EVENT_HANDLER != null) {
            Main.primaryStage.removeEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
            GP_MOVE_EVENT_HANDLER = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        this.startCellX = 0;
        this.startCellY = 0;

        graphics = gameCanvas.getGraphicsContext2D();

        // Exit dialog setup
        exitDialog = new Alert(Alert.AlertType.CONFIRMATION, getLocaleStr("dialogs.body.exit-from-current-level"),
                EXIT_OPTION, CANCEL_OPTION);
        exitDialog.setTitle(getLocaleStr("header.base") + " - " + getLocaleStr("exit"));
        exitDialog.setHeaderText(getLocaleStr("dialogs.head.exit-from-current-level"));

        // Adding CSS-Stylesheet to customize dialog, for example, fonts
        exitDialog.getDialogPane().getStylesheets().add(Main.getResourceURL("styles/bigger-dialog-fonts.css").toExternalForm());
        exitDialog.getDialogPane().getStyleClass().add("dialog-body");

        exitDialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        exitDialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        exitDialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void wakeUp() {
        currentLevel().getGrid().forEach(column -> column.forEach(levelCell -> levelCell.setVisited(false)));
        levelGridWidth = currentLevel().getGrid().size();
        levelGridHeight = currentLevel().getGrid().get(0).size();

        // Getting info about start cell
        for (int x = 0; x < levelGridWidth; x++) {
            for (int y = 0; y < levelGridHeight; y++) {
                if (currentLevel().getGrid().get(x).get(y).getType() == CellType.START) {
                    startCell = (StartCell) currentLevel().getGrid().get(x).get(y);
                    pointerX = startCellX = CELL_SIZE * x;
                    pointerY = startCellY = CELL_SIZE * y;
                }
            }
        }

        FieldDrawAnimation drawAnimation = new FieldDrawAnimation();
        drawAnimation.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        removeGPMovement();
        graphics.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        this.startTime = System.nanoTime();
        this.pointerX = 0;
        this.startCellX = 0;
        this.pointerY = 0;
        this.startCellY = 0;
        this.movingPointer = false;
        this.undoFlag = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.stepLines = new ArrayList<>();
        this.startCell = null;
    }

    /**
     * Redraws <b>one</b> cell on field.
     *
     * @param x x position of cell. <i><b>Note:</b> it's position in {@link Level#grid}, not in canvas.</i>
     * @param y y position of cell. <i><b>Note:</b> it's position in {@link Level#grid}, not in canvas.</i>
     */
    @SuppressWarnings("deprecation")
    private void redrawFieldCell(int x, int y) {
        LevelCell cell = currentLevel().getGrid().get(x).get(y);
        if (cell.getType() != CellType.START) cell.draw(x, y, graphics, this);
    }

    /**
     * Redraws all field.
     *
     */
    private void redrawField() {
        // Clear canvas and setup colors
        graphics.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(1);

        for (int x = 0; x < levelGridWidth; x++) {
            for (int y = 0; y < levelGridHeight; y++) {
                redrawFieldCell(x, y);
            }
        }
    }

    /**
     * Event handler for all buttons.
     *
     * @param event action event from button
     *
     */
    public void actionButtonPressed(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        switch (clickedButton.getId()) {
            case "endButton":
                Optional result = exitDialog.showAndWait();

                if (result.isPresent() && result.get() == EXIT_OPTION) {
                    removeGPMovement();
                    Main.changeScene("level-select.fxml");
                } else if (result.isPresent() && result.get() == CANCEL_OPTION) {
                    exitDialog.hide();
                }

                break;

            case "undoButton":
                doUndo();
                break;

            case "redoButton":
                doRedo();
                break;

            case "restartButton":
                removeGPMovement();
                this.shutdown();
                this.wakeUp();
                break;
        }
    }

    /**
     * Activates when you need to redo something.
     *
     * @see #undoStack
     * @see #redoStack
     *
     */
    private void doRedo() {
        if (!movingPointer) {
            Move move = redoStack.last();

            if (move != null) {
                startMovingPointer(move, StepCause.DOING_REDO);
            }
        }
    }

    /**
     * Activates when you need to undo something.
     *
     * @see #undoStack
     * @see #redoStack
     *
     */
    private void doUndo() {
        if (!movingPointer) {
            Move move = undoStack.last();

            if (move != null && stepLines.size() != 0) {
                stepLines.remove(stepLines.size() - 1);

                if (currentLevel().getGrid().get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).isVisited())
                    currentLevel().getGrid().get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).setVisited(false);

                // Inverting move
                switch (move) {
                    case UP:
                        move = Move.DOWN;
                        break;

                    case LEFT:
                        move = Move.RIGHT;
                        break;

                    case DOWN:
                        move = Move.UP;
                        break;

                    case RIGHT:
                        move = Move.LEFT;
                        break;
                }

                undoFlag = true;
                startMovingPointer(move, StepCause.DOING_UNDO);
            }
        }
    }

    /**
     * Enum with causes of steps.
     *
     * @see #KEY_WAS_PRESSED
     * @see #DOING_REDO
     * @see #DOING_UNDO
     */
    private enum StepCause {
        /** This cause is used when user presses key. */
        KEY_WAS_PRESSED,
        /** This cause is used when doing undo (in method {@link #doUndo()}) */
        DOING_UNDO,
        /** This cause is used when doing redo (in method {@link #doRedo()}) */
        DOING_REDO
    }

    /**
     * Shortcut for calling <code>Storage.getInstance().currentLevel</code>.
     *
     * @return value of {@link ExternalStorage#currentLevel}.
     */
    private Level currentLevel() {
        return ExternalStorage.getInstance().currentLevel;
    }

}
