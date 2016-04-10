package levels.cells;

import control.GamePlayController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import util.ExtendedMath;

import static control.GamePlayController.CELL_SIZE;

/**
 * Class for start cells.
 *
 * @see levels.cells.CellType#START
 *
 */
public class StartCell extends LevelCell {
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
    private void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, GraphicsContext graphics) {
        graphics.fillPolygon(new double[] {x1, x2, x3}, new double[] {y1, y2, y3}, 3);
        graphics.strokePolygon(new double[] {x1, x2, x3}, new double[] {y1, y2, y3}, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellType getType() {
        return CellType.START;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(int x, int y, GraphicsContext graphics, GamePlayController controller) {
        // This thing draws green triangle on start cell
        graphics.setFill(Color.GREEN);

        final double SHIFT = 25.0;
        final double x1 = ExtendedMath.map(SHIFT, 0.0, 100.0, 0.0, (double) CELL_SIZE);
        final double y1 = ExtendedMath.map(SHIFT, 0.0, 100.0, 0.0, (double) CELL_SIZE);
        final double x2 = ExtendedMath.map(100.0 - SHIFT + 5.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);
        final double y2 = ExtendedMath.map(100.0 / 2.0, 0.0, 100.0, 0.0, (double) CELL_SIZE);
        final double x3 = ExtendedMath.map(SHIFT, 0.0, 100.0, 0.0, (double) CELL_SIZE);
        final double y3 = ExtendedMath.map(100.0 - SHIFT, 0.0, 100.0, 0.0, (double) CELL_SIZE);
        drawTriangle(controller.startCellX + x1, controller.startCellY + y1,
                controller.startCellX + x2, controller.startCellY + y2,
                controller.startCellX + x3, controller.startCellY + y3,
                graphics);
    }
}
