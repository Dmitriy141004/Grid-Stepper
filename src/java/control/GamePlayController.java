package control;

import util.ExtendedMath;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import levels.LevelCell;
import levels.XMLLevelLoader;
import start.Main;
import util.LIFOQueue;
import util.Stack;
import util.Line2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Main game play controller. It's the most difficult. Here're all game logic.
 *
 * @author Dmitriy Meleshko
 * @since v. 1.0
 *
 */
public class GamePlayController extends FXController {
    /** Link to primary stage. Here it's used for attaching event handlers such as key handlers. */
    private Stage primaryStage = Main.primaryStage;

    /** Size of one cell. It's quite important constant, it's used when controller redraws game field.
     * @see #redrawField() */
    private static final int CELL_SIZE = 45;

    /** Name of this field says about itself - it's {@code false} when level isn't loaded, and {@code true} when
     * level is already loaded. It's used when redrawing field: if it's first redraw set {@link #pointerX} and {@link #pointerY}
     * to start values. */
    private boolean levelIsLoaded = false;
    /** This pointer points, on what <b>X co-ord</b> is it now. <i><b>Note:</b> it's a co-ord on canvas, not on level.</i> */
    private int pointerX;
    /** This pointer points, on what <b>Y co-ord</b> is it now. <i><b>Note:</b> it's a co-ord on canvas, not on level.</i> */
    private int pointerY;

    /** 2D grid of level.
     * @see XMLLevelLoader#innerLoadLevel(String, String) */
    private ArrayList<ArrayList<LevelCell>> level;
    /** Maximum size of game pointer. */
    private static final int GAME_POINTER_SIZE = 25;
    /** Color of game pointer. */
    private static final Color GAME_POINTER_COLOR = Color.rgb(67, 157, 28);
    /** Width of level. <i><b>Note:</b> it's size of grid, not of level.</i> */
    private int levelWidth;
    /** Height of level. <i><b>Note:</b> it's size of grid, not of level.</i> */
    private int levelHeight;
    /** This' variables name says about himself like {@link #levelIsLoaded}. It's used to make game pointer move just
     * in one direction on one keypress. */
    private boolean movingPointer = false;

    /** Game canvas from {@code .fxml} file. */
    @FXMLLink
    private Canvas gameCanvas;
    /** Link to {@link #gameCanvas}' graphics. Just for time economy. */
    private GraphicsContext graphics;

    /**
     * Undo stack for moves. When you press {@code Ctrl+Z} - this code runs:
     * <pre><code>
     * if (movingPointer) return;
     *
     * String move = undoStack.pop();
     *
     * if (move == null) return;
     * redoStack.push(move);
     *
     * if (movingPointer) return;
     *
     * pointerMoveAnimation = new PointerMoveAnimation(invertMove(move));
     * pointerMoveAnimation.start();
     * </code></pre>
     *
     * Where {@code invertMove(String)} is function, that inverts direction of move by this schema:
     *
     * <pre><code>
     * A &lt;----&gt; D
     * W &lt;----&gt; S
     * </code></pre>
     *
     * As you can see - {@link String Strings} are moves/key chars.
     *
     *  <p><i><b>Note:</b> this stack appends when you move.</i></p>
     */
    private LIFOQueue<String> undoStack = new Stack<>();
    /**
     * Undo stack for moves. When you press {@code Ctrl+R} - this code runs:
     * <pre><code>
     * if (movingPointer) return;
     *
     * String move = redoStack.pop();
     *
     * if (move == null) return;
     * undoStack.push(move);
     *
     *
     * pointerMoveAnimation = new PointerMoveAnimation(move);
     * pointerMoveAnimation.start();
     * </code></pre>
     *
     * Where {@code invertMove(String)} is function, that inverts direction of move by this schema:
     *
     * <pre><code>
     * A &lt;----&gt; D
     * W &lt;----&gt; S
     * </code></pre>
     *
     * As you can see - {@link String Strings} are moves/key chars.
     *
     *
     */
    private LIFOQueue<String> redoStack = new Stack<>();

    /** Exit dialog object. */
    private Alert exitDialog;
    /** Exit action of exit dialog. */
    private static final ButtonType EXIT_OPTION = new ButtonType(Main.getLocaleStr("exit"), ButtonBar.ButtonData.OK_DONE);
    /** Cancel action of exit dialog. */
    private static final ButtonType CANCEL_OPTION = new ButtonType(Main.getLocaleStr("cancel"),
            ButtonBar.ButtonData.CANCEL_CLOSE);

    /** Instance of {@link PointerMoveAnimation}. */
    private PointerMoveAnimation pointerMoveAnimation;
    /** Handler for key input. Extracted to object because I need to remove it from list of handlers when
     * {@link #actionButtonPressed(ActionEvent)} receives action from "end game" button. */
    private final EventHandler<KeyEvent> GP_MOVE_EVENT_HANDLER = (KeyEvent observable) -> {
        if (movingPointer) return;

        String keyText = observable.getText().toLowerCase();

        // Undo on Ctrl+Z
        if (observable.getCode().equals(KeyCode.Z) && observable.isControlDown() &&
                !observable.isShiftDown() && !observable.isAltDown()) {
            doUndo();
            return;
        }

        // Redo on Ctrl+R
        if (observable.getCode().equals(KeyCode.R) && observable.isControlDown() &&
                !observable.isShiftDown() && !observable.isAltDown()) {
            doRedo();
            return;
        }

        // If key isn't in group "W A S D" - return
        if (!keyText.equals("w") && !keyText.equals("a") && !keyText.equals("s") && !keyText.equals("d")) return;

        undoStack.push(keyText);
        redoStack.clear();
        pointerMoveAnimation = new PointerMoveAnimation(observable.getText());
        pointerMoveAnimation.start();
    };
    /** Lines that GP leaves after moving. */
    private ArrayList<Line2D> stepLines = new ArrayList<>(0);
    /** This flag is set when {@link #actionButtonPressed(ActionEvent)} receives event from undo button. */
    private boolean undoFlag = false;

    /**
     * This class implements abstract class {@link AnimationTimer}. Why I used this class? Firstly, I needed to create
     * animation of game pointer's move. I tried to use {@link javafx.animation.TranslateTransition}, but I found it
     * bad when you need to switch pointers. And drawing on canvas was the best solution, but I needed to redraw scene
     * every frame
     *
     */
    private class PointerMoveAnimation extends AnimationTimer {

        /**
         * Constructor is quite complicated. Firstly it sets {@link #movingPointer} to {@code true} - this says that we're
         * moving pointer. And, secondly this part sets two variables: {@link #goalX} and {@link #goalY}.
         *
         * <p><i><b>Note:</b> if "goal cell" is filled animation will be stopped.</i></p>
         *
         * @param keyText text of pressed key, used for recognizing direction.
         */
        PointerMoveAnimation(String keyText) {
            movingPointer = true;

            // Choosing right directions
            switch (keyText) {
                case "w":
                    if (pointerY == 0) {
                        stop();
                        break;
                    }

                    goalY = pointerY - CELL_SIZE;
                    break;

                case "a":
                    if (pointerX == 0) {
                        stop();
                        break;
                    }

                    goalX = pointerX - CELL_SIZE;
                    break;

                case "s":
                    if (pointerY / CELL_SIZE >= levelHeight - 1) {
                        stop();
                        break;
                    }

                    goalY = pointerY + CELL_SIZE;
                    break;

                case "d":
                    if (pointerX / CELL_SIZE >= levelWidth - 1) {
                        stop();
                        break;
                    }

                    goalX = pointerX + CELL_SIZE;
                    break;

                // If switch can't recognize key (it isn't in group "W A S D") - don't do anything
                default:
                    stop();
                    return;
            }

            if (level.get(goalX / CELL_SIZE).get(goalY / CELL_SIZE).getCellType() == LevelCell.CellType.WALL ||
                    (level.get(goalX / CELL_SIZE).get(goalY / CELL_SIZE).isVisited()) && !undoFlag) {
                stop();
            }

            // Are all cells visited?
            boolean allCellsVisited = true;

            for (ArrayList<LevelCell> levelPart : level) {
                for (LevelCell levelCell : levelPart) {
                    if (Arrays.asList(LevelCell.CellType.EMPTY, LevelCell.CellType.START).contains(levelCell.getCellType()) &&
                            !Arrays.asList(LevelCell.CellType.FINISH, LevelCell.CellType.WALL)
                                    .contains(levelCell.getCellType()) && !levelCell.isVisited()) allCellsVisited = false;
                }
            }

            if (level.get(goalX / CELL_SIZE).get(goalY / CELL_SIZE).getCellType() == LevelCell.CellType.FINISH
                    && allCellsVisited) System.out.println("Level completed!");
        }

        /** End position of pointer move on X axis. By default it equals {@link #pointerX}, but in
         * {@link #PointerMoveAnimation(String)}  constructor} it's changed by move value. */
        int goalX = pointerX;
        /** End position of pointer move on Y axis. By default it equals {@link #pointerY}, but in
         * {@link #PointerMoveAnimation(String) constructor} it's changed by move value. */
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
         * This method is an implementation of {@link AnimationTimer#handle(long)}. Here's
         * all move animation logic:
         *
         * <ol>
         *     <li><b>Changing position of pointer:</b> if {@link #goalX} is grater than {@link #pointerX} -
         *     last one increments, otherwise - decrements. Same this with {@link #goalY} and {@link #pointerY}.</li>
         *     <li><b>Stopping animation:</b> if reached end ({@code pointerX == goalX && pointerY == goalY}) -
         *     animation stops.</li>
         * </ol>
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
                graphics.strokeLine(startX + CELL_SIZE / 2, startY + CELL_SIZE / 2, pointerX + CELL_SIZE / 2, pointerY + CELL_SIZE / 2);
            } else {
                graphics.strokeLine(pointerX + CELL_SIZE / 2, pointerY + CELL_SIZE / 2, goalX + CELL_SIZE / 2, goalY + CELL_SIZE / 2);
            }

            // This draws another step lines
            stepLines.forEach(line -> graphics.strokeLine(line.getX1() + CELL_SIZE / 2, line.getY1() + CELL_SIZE / 2,
                    line.getX2() + CELL_SIZE / 2, line.getY2() + CELL_SIZE / 2));

            graphics.setLineWidth(1);

            // This thing draws GP
            graphics.setFill(GAME_POINTER_COLOR);
            graphics.fillOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                    GAME_POINTER_SIZE, GAME_POINTER_SIZE);
            graphics.strokeOval(pointerX + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2, pointerY + CELL_SIZE / 2 - GAME_POINTER_SIZE / 2,
                    GAME_POINTER_SIZE, GAME_POINTER_SIZE);

            if (pointerX == goalX && pointerY == goalY) {
                if (!undoFlag) stepLines.add(new Line2D(startX, startY, pointerX, pointerY));
                level.get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).setVisited(true);

                stop();
                System.out.printf("%15d\t|\t%15d\t|\t%15d\t|\t%15d\t|\t%s\t\n", pointerX, pointerY, levelWidth,
                        levelHeight, undoStack);
            }
        }

        /**
         * Overrides method {@link AnimationTimer#stop()} because there're some calls of this method before starting
         * animation. So, uses {@link #stopped variable-stopper}.
         *
         */
        @Override
        public void start() {
            if (!stopped) super.start();
        }

        /**
         * Overrides method {@link AnimationTimer#stop()}. Why I have chosen to override? That's because of
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
            stopped = true;
            super.stop();
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
         * This constructor sets {@link #length} to given value.
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
    private class FieldDrawAnimation extends AnimationTimer {
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
         * Implementation of {@link AnimationTimer#start()}. Here're all animation logic.
         *
         * @param now time in nanoseconds (same as {@link System#nanoTime()}, but with some additions)
         *
         */
        @Override
        public void handle(long now) {
            redrawFieldCell(x, y);

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
                setupAnimations();
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
     * Setups animations, such as controlling GP. Activates after end of field draw animation.
     *
     */
    private void setupAnimations() {
        // Event handler for moving GP
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
    }

    @Override
    public void init() {
        graphics = gameCanvas.getGraphicsContext2D();

        System.out.printf("%15s\t|\t%15s\t|\t%15s\t|\t%15s\t|\t%15s\t\n", "pointer x", "pointer y", "level width",
                "level height", "undo stack");

        // Loading level
        try {
            level = XMLLevelLoader.loadLevel(Main.getResource("levels/classic.xml"), "1");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        levelWidth = level.size();
        levelHeight = level.get(0).size();

        // Exit dialog setup
        exitDialog = new Alert(Alert.AlertType.CONFIRMATION, getLocaleStr("dialogs.body.exit-from-current-level"),
                EXIT_OPTION, CANCEL_OPTION);
        exitDialog.setTitle(getLocaleStr("header.base") + " - " + getLocaleStr("exit"));
        exitDialog.setHeaderText(getLocaleStr("dialogs.head.exit-from-current-level"));

        // Adding CSS-Stylesheet to customize dialog, for example, fonts
        exitDialog.getDialogPane().getStylesheets().add(Main.getResourceURL("styles/bigger-dialog-fonts.css").toExternalForm());
        exitDialog.getDialogPane().getStyleClass().add("dialog-body");

        // And, customizing dialog with setters
        Label contentLabel = (Label) exitDialog.getDialogPane().lookup(".content");
        contentLabel.setWrapText(true);

        exitDialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        exitDialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        exitDialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);

        FieldDrawAnimation drawAnimation = new FieldDrawAnimation();
        drawAnimation.start();
    }

    /**
     * Draws finish cell. Finish is 4x4 grid in cell. And, for drawing finish columns as normal, I use variable
     * {@code fillColumnShift}. When loop for {@code y} axis starts, variable {@code fillBlack} sets to shift variable.
     * When this loop ends, {@code fillColumnShift} inverts.
     *
     * @param x x of cell
     * @param y y of cell
     */
    private void drawFinish(int x, int y) {
        boolean fillColumnShift = true;

        for (int x1 = 0; x1 < 4; x1++) {
            boolean fillBlack = fillColumnShift;

            for (int y1 = 0; y1 < 4; y1++) {
                if (fillBlack) graphics.setFill(Color.BLACK);
                else graphics.setFill(Color.WHITE);
                fillBlack = !fillBlack;

                graphics.fillRect(x * CELL_SIZE + x1 * (CELL_SIZE / 4) + 1, y * CELL_SIZE + y1 * (CELL_SIZE / 4) + 1,
                        CELL_SIZE / 4, CELL_SIZE / 4);
                graphics.strokeRect(x * CELL_SIZE + x1 * (CELL_SIZE / 4) + 1, y * CELL_SIZE + y1 * (CELL_SIZE / 4) + 1,
                        CELL_SIZE / 4, CELL_SIZE / 4);
            }

            fillColumnShift = !fillColumnShift;
        }
    }

    /**
     * Draws triangle. It's wrapper for such code:
     * <pre><code>
     * graphics.fillPolygon(new double[] {x1, x2, x3}, new double[] {y1, y2, y3}, 3);
     * graphics.strokePolygon(new double[] {x1, x2, x3}, new double[] {y1, y2, y3}, 3);
     * </code></pre>
     * Well, it's quite long construction. This method makes this code much smaller:
     * <pre><code>
     * drawTriangle(x1, y1, x2, y2, x3, y3);
     * </code></pre>
     * Looks smaller, isn't it?
     *
     * @param x1 first {@code x} coordinate
     * @param y1 first {@code y} coordinate
     * @param x2 second {@code x} coordinate
     * @param y2 second {@code y} coordinate
     * @param x3 third {@code x} coordinate
     * @param y3 third {@code y} coordinate
     */
    private void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        graphics.fillPolygon(new double[] {x1, x2, x3}, new double[] {y1, y2, y3}, 3);
        graphics.strokePolygon(new double[] {x1, x2, x3}, new double[] {y1, y2, y3}, 3);
    }

    /**
     * Redraws <b>one</b> cell on field.
     *
     * <p>Starting cell is peculiar (see {@link levels.LevelCell.CellType#START} to read how it looks). To draw triangle
     * on the start in use this scheme:<br>
     * 1. Using some value in range 0.0 - 100.0 for current point (start - {@code x1}),<br>
     * 2. Mapping this value to range 0.0 - {@value CELL_SIZE}.0,<br>
     * 3. Giving received value to method that draws triangle,<br>
     * 4. Repeating this 5 times with {@code y1}, {@code x2}, {@code y2}, {@code x3} and {@code y3}.</p>
     *
     * @param x x position of cell. <i><b>Note:</b> it's position in {@link #level collection}, not in canvas.</i>
     * @param y y position of cell. <i><b>Note:</b> it's position in {@link #level collection}, not in canvas.</i>
     */
    @SuppressWarnings("deprecation")
    private void redrawFieldCell(int x, int y) {
        switch (level.get(x).get(y).getCellType()) {
            case EMPTY:
                graphics.setFill(Color.WHITE);
                graphics.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                graphics.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                break;

            case WALL:
                graphics.setFill(Color.BLACK);
                graphics.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                break;

            case START:
                if (!levelIsLoaded) {
                    pointerX = x * CELL_SIZE;
                    pointerY = y * CELL_SIZE;
                }

                graphics.setFill(Color.GREEN);
                                                                                              // double SHIFT = 25.0
                double x1 = ExtendedMath.map(25.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);      // SHIFT
                double y1 = ExtendedMath.map(25.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);      // SHIFT
                double x2 = ExtendedMath.map(80.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);      // 100.0 - SHIFT + 5.0
                double y2 = ExtendedMath.map(50.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);      // 100.0 / 2.0
                double x3 = ExtendedMath.map(25.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);      // SHIFT
                double y3 = ExtendedMath.map(75.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);      // 100.0 - SHIFT
                drawTriangle(pointerX + x1, pointerY + y1, pointerX + x2, pointerY + y2, pointerX + x3, pointerY + y3);
                break;

            case FINISH:
                drawFinish(x, y);
                break;
        }

        graphics.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
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

        levelIsLoaded = true;
    }

    /**
     * Event handler for all buttons. Moved in one method because of {@link ActionEvent} as param - it was not used.
     *
     * @param event action event from button
     *
     */
    public void actionButtonPressed(ActionEvent event) {
        Object source = event.getSource();

        // if source isn't button - we don't need to do anything else
        if (!(source instanceof Button)) {
            return;
        }

        Button clickedButton = (Button) source;

        switch (clickedButton.getId()) {
            case "endButton":
                Optional result = exitDialog.showAndWait();

                if (result.isPresent() && result.get() == EXIT_OPTION) {
                    primaryStage.removeEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
                    Main.changeScene("main.fxml", getLocaleStr("header.base"));
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
                primaryStage.removeEventHandler(KeyEvent.KEY_PRESSED, GP_MOVE_EVENT_HANDLER);
                Main.changeScene("game-field.fxml", getLocaleStr("header.base"));
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
        if (movingPointer) return;

        String move = redoStack.last();

        if (move == null) {
            return;
        }

        redoStack.pop();

        undoStack.push(move);

        pointerMoveAnimation = new PointerMoveAnimation(move);
        pointerMoveAnimation.start();
    }

    /**
     * Activates when you need to undo something.
     *
     * @see #undoStack
     * @see #redoStack
     *
     */
    private void doUndo() {
        if (movingPointer) return;

        String move = undoStack.last();

        if (move == null || stepLines.size() == 0) return;

        undoStack.pop();

        redoStack.push(move);

        stepLines.remove(stepLines.size() - 1);

        if (level.get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).isVisited())
            level.get(pointerX / CELL_SIZE).get(pointerY / CELL_SIZE).setVisited(false);

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
        pointerMoveAnimation = new PointerMoveAnimation(move);
        pointerMoveAnimation.start();
    }
}
