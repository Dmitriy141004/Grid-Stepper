package levels.cells;

import javafx.scene.canvas.GraphicsContext;
import mvc.controllers.GamePlayController;

/**
 * Enum with types of cells.
 */
public enum CellType {
    /**
     * Cell type for walls. You can't step on walls, and they're black squares.
     */
    WALL,
    /**
     * Cell type for empty cells. You can walk/step on them, but only <b>one</b> time. They're white squares.
     */
    EMPTY,
    /**
     * Cell type for start cell. You start from this cell. It is white square with green triangle inside.
     */
    START,
    /**
     * Cell type for finish cell. If you have stepped on all empty cells and then step on this one, level is completed.
     */
    FINISH,
    /**
     * Cell type for background squares. This cells are fully equal to walls, but method
     * {@link BackgroundSquare#draw(int, int, GraphicsContext, GamePlayController)} clears rectangle and doesn't draw
     * anything.
     */
    BACKGROUND_SQUARE
}
