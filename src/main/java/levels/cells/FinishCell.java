package levels.cells;

import mvc.controllers.GamePlayController;
import javafx.scene.canvas.GraphicsContext;

/**
 * Class for finish cells.
 *
 * @see levels.cells.CellType#FINISH
 *
 */
public class FinishCell extends LevelCell {
    /**
     * {@inheritDoc}
     */
    @Override
    public CellType getType() {
        return CellType.FINISH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(int x, int y, GraphicsContext graphics, GamePlayController controller) {

    }
}
