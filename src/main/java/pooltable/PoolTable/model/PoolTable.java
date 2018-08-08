package pooltable.PoolTable.model;

import java.util.ArrayList;

/**
 * PoolTable class
 */
public class PoolTable {
    private ArrayList<Ball> balls;
    private Cue cue;

    /**
     * PoolTable constructor
     *
     * @param balls Array of Balls
     * @param cue Cue
     */
    public PoolTable(ArrayList<Ball> balls, Cue cue) {
        this.balls = balls;
        this.cue = cue;
    }

    /**
     * Get Balls
     *
     * @return Balls
     */
    public ArrayList<Ball> getBalls() {
        return balls;
    }

    /**
     * Set Balls
     *
     * @param balls Balls
     */
    public void setBalls(ArrayList<Ball> balls) {
        this.balls = balls;
    }

    /**
     * Get Cue
     *
     * @return Cue
     */
    public Cue getCue() {
        return cue;
    }

    /**
     * Set Cue
     *
     * @param cue Cue
     */
    public void setCue(Cue cue) {
        this.cue = cue;
    }
}
