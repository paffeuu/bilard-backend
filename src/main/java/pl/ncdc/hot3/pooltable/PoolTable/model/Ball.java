package pl.ncdc.hot3.pooltable.PoolTable.model;


import org.opencv.core.Point;

/**
 * Ball class
 */
public class Ball implements Comparable<Ball> {
    private int id;
    public static final int DEFAULT_ID = 1;
    private double radius;
    private double x;
    private double y;

    @Override
    public int compareTo(Ball b) {
        if(this.id < b.id) {
            return -1;
        } else if(b.id < this.id) {
            return 1;
        }else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Ball{" +
                "id=" + id +
                ", radius=" + radius +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public double getWhitePixels() {
        return whitePixels;
    }

    public void setWhitePixels(double whitePixels) {
        this.whitePixels = whitePixels;
    }

    private double whitePixels;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }



    /**
     * Ball Constructor
     *
     * @param x  position X
     * @param y  position Y
     */
    public Ball(double x, double y, double radius) {
        this.id = DEFAULT_ID;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Get Ball id
     *
     * @return Ball id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set Ball id
     *
     * @param id Ball id
     */
    public void setId(Integer id) {
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

        double xDelta = Math.abs(this.x - b.getX());
        double yDelta = Math.abs(this.y - b.getY());

        return (xDelta <= 5.0 && yDelta <= 5.0);
    }

    public Point getCenter() {
        return new Point(x, y);
    }

    public void setCenter(Point point) {
        this.x = point.x;
        this.y = point.y;
    }
}
