package mvc.controllers;

import com.sun.javafx.geom.Line2D;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import levels.Level;
import levels.cells.CellType;
import levels.cells.LevelCell;
import mvc.util.ExternalStorage;
import mvc.util.FXController;
import start.Main;
import util.javafx.animation.ExtendedAnimationTimer;
import util.collections.LIFOQueue;
import util.javafx.scenes.SceneContent;
import util.collections.Stack;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Main game play controller. It's the most difficult. Here're all game logic.
 *
 */
public class GamePlayController extends FXController {
    /** Link to primary stage. Here it's used for attaching event handlers such as key handlers. */
    private volatile Stage primaryStage = Main.primaryStage;

    /** Size of one cell. It's quite important constant, it's used when controller redraws game field.
     * @see #redrawField() */
    public static final int CELL_SIZE = 45;

    /** This pointer points, on what <b>X coordinate</b> is it now. <i><b>Note:</b> it's a co-ord on canvas, not on level.
     * </i> */
    private volatile int pointerX;
    /** This pointer points, on what <b>Y coordinate</b> is it now. <i><b>Note:</b> it's a co-ord on canvas, not on level.
     * </i> */
    private volatile int pointerY;

    /** Maximum size of game pointer. */
    private static final int GAME_POINTER_SIZE = 25;
    /** Color of game pointer. */
    private static final Color GAME_POINTER_COLOR = Color.rgb(67, 157, 28);
    /** Width of level. <i><b>Note:</b> it's size of grid, not of level.</i> */
    private volatile int levelWidth;
    /** Height of level. <i><b>Note:</b> it's size of grid, not of level.</i> */
    private volatile int levelHeight;
    /** This' variables name says about himself. It's used to make game pointer move just
     * in one direction on one keypress. */
    private volatile boolean movingPointer = false;

    /** Game canvas from {@code .fxml} file. */
    @FXML
    private volatile Canvas gameCanvas;
    /** Link to {@link #gameCanvas}' graphics. Just for time economy. */
    private volatile GraphicsContext graphics;

    /**
     * Undo stack for moves. When you press {@code Ctrl+Z} or use button "Undo" - last move from this stack moves to
     * {@link #redoStack}. And also, this move executes.
     *
     * <p><i><b>Note:</b> this stack appends when you move.</i></p>
     *
     * @see #doUndo()
     * @see #doRedo()
     *
     */
    private volatile LIFOQueue<String> undoStack = new Stack<>();
    /**
     * Redo stack for moves. When you press {@code Ctrl+R} or use button "Redo" - last move from this stack moves to
     * {@link #undoStack}. And also, this move executes.
     *
     * @see #doUndo()
     * @see #doRedo()
     *
     */
    private volatile LIFOQueue<String> redoStack = new Stack<>();

    /** Exit dialog object. */
    private volatile Alert exitDialog;
    /** Exit action of exit dialog. */
    private static final ButtonType EXIT_OPTION = new ButtonType(Main.getLocaleStr("exit"), ButtonBar.ButtonData.YES);
    /** Cancel action of exit dialog. */
    private static final ButtonType CANCEL_OPTION = new ButtonType(Main.getLocaleStr("cancel"), ButtonBar.ButtonData.NO);

    /** Handler for key input. Extracted to object because I need to remove it from list of handlers when
     * {@link #actionButtonPressed(ActionEvent)} receives action from "end game" button. */
    private volatile EventHandler<KeyEvent> GP_MOVE_EVENT_HANDLER = null;
    /** Lines that GP leaves after moving. */
    private volatile ArrayList<Line2D> stepLines = new ArrayList<>(0);
    /** This flag is set when {@link #actionButtonPressed(ActionEvent)} receives event from undo button. */
    private volatile boolean undoFlag = false;
    /** {@code X} position of starting cell. */
    public volatile int startCellX;
    /** {@code Y} position of starting cell. */
    public volatile int startCellY;

    private volatile double startTime = System.nanoTime();

    private volatile LevelCell startCell;

    /**
     * This class implements abstract class {@link ExtendedAnimationTimer}. Why I used this class? Firstly, I needed to create
     * animation of game pointer's move. I tried to use {@link javafx.animation.TranslateTransition}, but I found it
     * bad when you need to switch pointers. And drawing on canvas was the best solution, but I needed to redraw scene
     * every frame.
     *
     */
    private class PointerMoveAnimation extends ExtendedAnimationTimer {

        /**
         * Constructor is quite complicated. Firstly it sets {@link #movingPointer} to {@code true} - this says that we're
         * moving pointer. And, secondly this part sets two variables: {@link #goalX} and {@link #goalY}.
         *
         * <p><i><b>Note:</b> if "goal cell" is wall animation will be stopped.</i></p>
         *
         * @param keyText text of pressed key, used for recognizing direction.
         * @param stepCause says "What caused this step?", and used to recognize "What to do with {@link #undoStack undo}
         *                  and {@link #redoStack redo} stacks?"
         */
        PointerMoveAnimation(String keyText, StepCause stepCause) {
            movingPointer = true;

            // Choosing right directions
            switch (keyText) {
                case "w":
                    if (pointerY == 0) {
                        stop();
                        return;
                    }

                    goalY = pointerY - CELL_SIZE;
                    break;

                case "a":
                    if (pointerX == 0) {
                        stop();
                        return;
                    }

                    goalX = pointerX - CELL_SIZE;
                    break;

                case "s":
                    if (pointerY >= (levelHeight - 1) * CELL_SIZE) {
                        stop();
                        return;
                    }

                    goalY = pointerY + CELL_SIZE;
                    break;

                case "d":
                    if (pointerX >= (levelWidth - 1) * CELL_SIZE) {
                        stop();
                        return;
                    }

                    goalX = pointerX + CELL_SIZE;
                    break;
            }

            LevelCell goalCell = currentLevel().getGrid().get(goalX / CELL_SIZE).get(goalY / CELL_SIZE);
            if (goalCell.getType() == CellType.WALL || goalCell.getType() == CellType.BACKGROUND_SQUARE ||
                    (goalCell.isVisited()) && !undoFlag) {
                stop();
                return;
            }

            // Thinking, what to do with stacks (if reached this place, step will be done)
            switch (stepCause) {
                case KEY_WAS_PRESSED:
                    undoStack.push(keyText);
                    redoStack.clear();
                    break;

                case DOING_REDO:
                    redoStack.pop();
                    undoStack.push(keyText);
                    break;

                case DOING_UNDO:
                    undoStack.pop();
                    redoStack.push(keyText);
                    break;
            }
        }

        /** End position of pointer move on X axis. By default it equals {@link #pointerX}, but in
         * {@link #PointerMoveAnimation(String, StepCause)}  constructor} it's changed by move value. */
        int goalX = pointerX;
        /** End position of pointer move on Y axis. By default it equals {@link #pointerY}, but in
         * {@link #PointerMoveAnimation(String, StepCause) constructor} it's changed by move value. */
        int goalY = pointerY;
        /** Speed ({@code pixels/frame}) of pointer movement. It can be only a multiple of {@code 3} or {@code 5}, and also
         * it can equal {@code 1}. */
        final int MOVE_STEP = 3;

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

        /**
         * This method is an implementation of {@link ExtendedAnimationTimer#handle(long)}. Here's
         * all move animation logic:
         *
         * <ol>
         *     <li><b>Changing position of pointer:</b> if {@link #goalX} is grater than {@link #pointerX} -
         *     last one increments, otherwise - decrements. Same this with {@link #goalY} and {@link #pointerY}.</li>
         *     <li><b>Stopping animation:</b> if reached end ({@code pointerX == goalX && pointerY == goalY}) -
         *     animation stops.</li>
         * </ol>
         *
         * <p>Starting cell is peculiar (see {@link levels.cells.CellType#START} to read how it looks).
         * Firstly, it must be on smaller layer than GP, but higher than lines, that GP leaves. Secondly, to draw triangle
         * on the start in use this scheme:
         * <ol>
         *     <li>Using some value in range 0.0 - 100.0 for current point (start - {@code x1})</li>
         *     <li>Mapping this value to range 0.0 - {@value mvc.controllers.GamePlayController#CELL_SIZE}.0</li>
         *     <li>Giving received value to method that draws triangle</li>
         *     <li>Repeating with {@code y1}, {@code x2}, {@code y2}, {@code x3} and {@code y3}</li>
         * </ol></p>
         *
         * @param now time in nanoseconds (same as {@link System#nanoTime()}, but with some additions)
         */
        @Override
        public void handle(long now) {
            if (pointerX < goalX) pointerX += MOVE_STEP;
            if (pointerY < goalY) pointerY += MOVE_STEP;
            if (pointerX > goalX) pointerX -= MOVE_STEP;
            if (pointerY > goalY) pointerY -= MOVE_STEP;

            redrawField();

            graphics.setStroke(Color.BLACK);
            graphics.setLineWidth(4);

            if (!undoFlag) {
                // This draws current step line, GP will be over this line
                graphics.strokeLine(startX + CELL_SIZE / 2, startY + CELL_SIZE / 2,
                        pointerX + CELL_SIZE / 2, pointerY + CELL_SIZE / 2);
            } else {
                graphics.strokeLine(pointerX + CELL_SIZE / 2, pointerY + CELL_SIZE / 2,
                        goalX + CELL_SIZE / 2, goalY + CELL_SIZE / 2);
            }

            // This draws another step lines
            stepLines.forEach(line -> graphics.strokeLine(line.x1 + CELL_SIZE / 2, line.y1 + CELL_SIZE / 2,
                    line.x2 + CELL_SIZE / 2, line.y2 + CELL_SIZE / 2));

            graphics.setLineWidth(1);

            // This thing draws green triangle on start cell
            startCell.draw(startCellX, startCellY, graphics, GamePlayController.this);

            // This thing draws GP
            graphics.setFill(GAME_POINTER_COLOR);
            graphics.fillOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                    GAME_POINTER_SIZE, GAME_POINTER_SIZE);
            graphics.strokeOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                    GAME_POINTER_SIZE, GAME_POINTER_SIZE);

            // If we reached goal coordinates...
            if (pointerX == goalX && pointerY == goalY) {
                // If undo flag is null, we add new line to stepLines
                if (!undoFlag) stepLines.add(new Line2D(startX, startY, pointerX, pointerY));
                currentLevel().getGrid().get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).setVisited(true);

                // Are all cells visited?
                boolean allCellsAreVisited = true;
                // To see this, we must iterate all rows in each column
                for (ArrayList<LevelCell> levelPart : currentLevel().getGrid()) {
                    // And each cell in each row
                    for (LevelCell levelCell : levelPart) {
                        // If cell is neither empty nor starting cell nor finish and it isn't visited -
                        // level isn't completed
                        if (Arrays.asList(CellType.EMPTY,
                                CellType.FINISH).contains(levelCell.getType())
                                && !levelCell.isVisited()) {
                            allCellsAreVisited = false;
                            break;
                        }
                    }
                }

                // If all cells are visited and current cell is finish...
                if (currentLevel().getGrid().get(goalX / CELL_SIZE).get(goalY / CELL_SIZE).getType() == CellType.FINISH
                        && allCellsAreVisited) {
                    // Level is completed!

                    // This thing will be activated after timer will be stopped
                    // I use this because if ExtendedAnimationTimer isn't stopped, you can't show new stage
                    // And my own implementation allows to do this--*
                    setEndAction(() -> {
                        removeGPMoving();

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
                        controller.reset();
                        controller.run();
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
     * @throws RuntimeException if catches {@link InterruptedException}, and sets cause to caught {@link InterruptedException}.
     */
    private void delay(long howLong) {
        try {
            Thread.sleep(howLong);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void startMovingPointer(String keyText, StepCause stepCause) {
        // Starting GP move animation
        PointerMoveAnimation pointerMoveAnimation = new PointerMoveAnimation(keyText, stepCause);
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
            if (x >= levelWidth - 1 && y >= levelHeight - 1 && animationState == null) {
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
                setupGPMoving();
                stop();
                return;
            }

            // This's "for loop"
            y++;
            if (y > levelHeight - 1) {
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
    private void setupGPMoving() {
        GP_MOVE_EVENT_HANDLER = (KeyEvent event) -> {
            if (!movingPointer) {
                String keyText = event.getText().toLowerCase();

                if (event.getCode().equals(KeyCode.Z) && event.isControlDown() &&
                        !event.isShiftDown() && !event.isAltDown()) {
                    // Undo on Ctrl+Z
                    doUndo();
                } else if (event.getCode().equals(KeyCode.R) && event.isControlDown() &&
                        !event.isShiftDown() && !event.isAltDown()) {
                    // Redo on Ctrl+R
                    doRedo();
                } else if ("w".equals(keyText) || "a".equals(keyText) || "s".equals(keyText) || "d".equals(keyText))
                    // If key is in group "W A S D" - moving GP
                    startMovingPointer(event.getText(), StepCause.KEY_WAS_PRESSED);
            }
        };
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
    }

    /**
     * Removes {@link KeyEvent} handler for moving GP.
     *
     */
    private void removeGPMoving() {
        if (GP_MOVE_EVENT_HANDLER != null) {
            primaryStage.removeEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
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
    public void run() {
        currentLevel().getGrid().forEach(column -> column.forEach(levelCell -> levelCell.setVisited(false)));
        levelWidth = currentLevel().getGrid().size();
        levelHeight = currentLevel().getGrid().get(0).size();

        // Getting info about start cell
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                if (currentLevel().getGrid().get(x).get(y).getType() == CellType.START) {
                    startCell = currentLevel().getGrid().get(x).get(y);
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
    public void reset() {
        removeGPMoving();
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

        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
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
                    removeGPMoving();
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
                removeGPMoving();
                this.reset();
                this.run();
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
            String move = redoStack.last();

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
            String move = undoStack.last();

            if (move != null && stepLines.size() != 0) {
                stepLines.remove(stepLines.size() - 1);

                if (currentLevel().getGrid().get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).isVisited())
                    currentLevel().getGrid().get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).setVisited(false);

                // Inverting move
                switch (move) {
                    case "w":
                        move = "s";
                        break;

                    case "s":
                        move = "w";
                        break;

                    case "a":
                        move = "d";
                        break;

                    case "d":
                        move = "a";
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
