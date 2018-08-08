package pooltable.PoolTable.models;

/**
 * Cue class
 */
public class Cue {
    private double[] x;
    private double[] y;

    /**
     * Cue constructor
     *
     * @param x Cue x position (start, end)
     * @param y Cue y position (start, end)
     */
    public Cue(double[] x, double[] y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get Cue x position
     *
     * @return Cue x position
     */
    public double[] getX() {
        return x;
    }

    /**
     * Set Cue x position
     *
     * @param x Cue x position
     */
    public void setX(double[] x) {
        this.x = x;
    }

    /**
     * Get Cue y position
     *
     * @return Cue y position
     */
    public double[] getY() {
        return y;
    }

    /**
     * Set Cue y position
     *
     * @param y Cue y position
     */
    public void setY(double[] y) {
        this.y = y;
    }
}
