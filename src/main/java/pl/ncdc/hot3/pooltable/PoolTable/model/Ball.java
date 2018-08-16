package pl.ncdc.hot3.pooltable.PoolTable.model;

/**
 * Ball class
 */
public class Ball {
    private int id;
    private double x;
    private double y;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    private double radius;

    /**
     * Ball Constructor
     *
     * @param id Ball number (0 - cue ball)
     * @param x  position X
     * @param y  position Y
     */
    public Ball(int id, double x, double y, double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ball)) {
            return false;
        }

        Ball b = (Ball) o;

        double xDelta = this.x - b.getX();
        double yDelta = this.y - b.getY();

        return (xDelta <= 15.0 && yDelta <= 15.0);
    }
}
