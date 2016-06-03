package levels.cells;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mvc.controllers.gameplay.GamePlayController;

import static mvc.controllers.gameplay.GamePlayController.CELL_SIZE;

/**
 * Class for empty cells.
 *
 * @see levels.cells.CellType#EMPTY
 */
public class EmptyCell extends LevelCell {
    static final Color CELL_BACKGROUND_COLOR = Color.grayRgb(240);

    /**
     * {@inheritDoc}
     */
    @Override
    public CellType getType() {
        return CellType.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(int x, int y, GraphicsContext graphics, GamePlayController controller) {
        graphics.setFill(EmptyCell.CELL_BACKGROUND_COLOR);
        graphics.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        graphics.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}
