package bin.util;

/**
 * Class for storing simple line on 2D canvas. Doesn't support vectors.
 *
 */
public class Line2D {
    /** First {@code x} position. */
    private int x1;
    /** First {@code y} position. */
    private int y1;
    /** Second {@code x} position. */
    private int x2;
    /** Second {@code y} position. */
    private int y2;

    /**
     * Getter for {@link #x1}.
     *
     * @return value of {@link #x1}.
     */
    public int getX1() {
        return x1;
    }
    /**
     * Getter for {@link #y1}.
     *
     * @return value of {@link #y1}.
     */
    public int getY1() {
        return y1;
    }
    /**
     * Getter for {@link #x2}.
     *
     * @return value of {@link #x2}.
     */
    public int getX2() {
        return x2;
    }
    /**
     * Getter for {@link #y2}.
     *
     * @return value of {@link #y2}.
     */
    public int getY2() {
        return y2;
    }

    /**
     * Simple constructor for class. Just sets fields {@link #x1}, {@link #x2}, {@link #y1} and {@link #y2}.
     *
     * @param x1 new value of {@link #x1}
     * @param y1 new value of {@link #y1}
     * @param x2 new value of {@link #x2}
     * @param y2 new value of {@link #y2}
     */
    public Line2D(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
