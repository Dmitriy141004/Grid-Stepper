package levels.cells;

import javafx.scene.canvas.GraphicsContext;
import mvc.controllers.gameplay.GamePlayController;

import static mvc.controllers.gameplay.GamePlayController.CELL_SIZE;

/**
 * Cell type for background squares. This cells are fully equal to walls, but method
 * {@link #draw(int, int, GraphicsContext, GamePlayController)} clears rectangle and doesn't draw anything.
 *
 * @see levels.cells.CellType#WALL
 */
public class BackgroundSquare extends LevelCell {
    /**
     * {@inheritDoc}
     */
    @Override
    public CellType getType() {
        return CellType.BACKGROUND_SQUARE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(int x, int y, GraphicsContext graphics, GamePlayController controller) {
        graphics.clearRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        graphics.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}
