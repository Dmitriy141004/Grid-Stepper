package bin.levels;

/**
 * Class for storing data about each level
 *
 */
public class LevelCell {
    public enum CellType {
        WALL, EMPTY, START, FINISH
    }

    private CellType cellType;
    private boolean visited = false;

    public LevelCell(CellType cellType) {
        this.cellType = cellType;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isVisited() {
        return visited;
    }

    public CellType getCellType() {
        return cellType;
    }
}
