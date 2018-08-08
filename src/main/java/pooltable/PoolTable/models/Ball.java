package pooltable.PoolTable.models;

/**
 * Ball class
 */
public class Ball {
    private int id;
    private double x;
    private double y;

    /**
     * Ball Constructor
     *
     * @param id Ball number (0 - cue ball)
     * @param x  position X
     * @param y  position Y
     */
    public Ball(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    /**
     * Get Ball id
     *
     * @return Ball id
     */
    public int getId() {
        return id;
    }

    /**
     * Set Ball id
     *
     * @param id Ball id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get Ball x position
     *
     * @return Ball x position
     */
    public double getX() {
        return x;
    }

    /**
     * Set Ball x position
     *
     * @param x Ball x position
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Get Ball y position
     *
     * @return Ball y position
     */
    public double getY() {
        return y;
    }

    /**
     * Set Ball y position
     *
     * @param y Ball y position
     */
    public void setY(double y) {
        this.y = y;
    }
}
