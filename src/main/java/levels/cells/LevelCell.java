package levels.cells;

import mvc.controllers.GamePlayController;
import javafx.scene.canvas.GraphicsContext;

/**
 * Class for storing data about <b>one</b> cell in level.
 *
 */
public abstract class LevelCell {
    /** Flag for visited cells. */
    private boolean visited = false;

    /**
     * Setter for field {@link #visited}.
     *
     * @param visited new value of field
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Getter for field {@link #visited}.
     *
     * @return value of field {@link #visited}
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * Returns type of current cell. Used to avoid {@code instanceof} operator.
     *
     * @return type of current cell. Used to avoid {@code instanceof} operator.
     */
    public abstract CellType getType();

    /**
     * Abstract method that will draw cell.
     * @param x {@code x} position on {@link javafx.scene.canvas.Canvas}
     * @param y {@code y} position on {@link javafx.scene.canvas.Canvas}
     * @param graphics {@link GraphicsContext} from {@link GamePlayController#graphics}
     * @param controller {@link GamePlayController} that called method
     *
     */
    public abstract void draw(int x, int y, GraphicsContext graphics, GamePlayController controller);
}
