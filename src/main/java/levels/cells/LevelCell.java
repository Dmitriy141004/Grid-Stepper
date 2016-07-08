package levels.cells;

import javafx.scene.canvas.GraphicsContext;
import mvc.controllers.gameplay.GamePlayController;

/**
 * Class for storing data about <b>one</b> cell in level.
 */
public abstract class LevelCell {
    private boolean visited = false;
    public boolean isVisited() {
        return visited;
    }
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Returns type of current cell. Used to avoid {@code instanceof} operator.
     *
     * @return type of current cell. Used to avoid {@code instanceof} operator.
     */
    public abstract CellType getType();

    /**
     * Abstract method that will draw cell.
     *
     * @param x          {@code x} position on {@link javafx.scene.canvas.Canvas}
     * @param y          {@code y} position on {@link javafx.scene.canvas.Canvas}
     * @param graphics   {@link GraphicsContext} from {@link GamePlayController#graphics}
     * @param controller {@link GamePlayController} that called method
     */
    public abstract void draw(int x, int y, GraphicsContext graphics, GamePlayController controller);
}
