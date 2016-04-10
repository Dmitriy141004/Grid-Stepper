package levels.cells;

import control.GamePlayController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static control.GamePlayController.CELL_SIZE;

/**
 * Class for walls.
 *
 * @see levels.cells.CellType#WALL
 *
 */
public class WallCell extends LevelCell {
    /**
     * {@inheritDoc}
     */
    @Override
    public CellType getType() {
        return CellType.WALL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(int x, int y, GraphicsContext graphics, GamePlayController controller) {
        graphics.setFill(Color.grayRgb(40));
        graphics.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        graphics.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
}
