package levels.cells;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mvc.controllers.GamePlayController;

import static mvc.controllers.GamePlayController.CELL_SIZE;

/**
 * Class for finish cells.
 *
 * @see levels.cells.CellType#FINISH
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
        boolean fillBlack = true;

        for (int xCount = 0; xCount < 4; xCount++) {
            for (int yCount = 0; yCount < 4; yCount++) {
                if (fillBlack)
                    graphics.setFill(Color.BLACK);
                else
                    graphics.setFill(EmptyCell.CELL_BACKGROUND_COLOR);
                fillBlack = !fillBlack;

                graphics.fillRect(x * CELL_SIZE + xCount * (CELL_SIZE / 4) + 1,
                        y * CELL_SIZE + yCount * (CELL_SIZE / 4) + 1,
                        CELL_SIZE / 4,
                        CELL_SIZE / 4);
                graphics.strokeRect(x * CELL_SIZE + xCount * (CELL_SIZE / 4) + 1,
                        y * CELL_SIZE + yCount * (CELL_SIZE / 4) + 1,
                        CELL_SIZE / 4,
                        CELL_SIZE / 4);
            }

            fillBlack = !fillBlack;
        }
    }
}
