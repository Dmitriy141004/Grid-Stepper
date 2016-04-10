package levels.cells;

import control.GamePlayController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static control.GamePlayController.CELL_SIZE;

/**
 * Class for empty cells.
 *
 * @see levels.cells.CellType#EMPTY
 *
 */
public class EmptyCell extends LevelCell {
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
        graphics.setFill(Color.WHITE);
        graphics.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        graphics.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}
