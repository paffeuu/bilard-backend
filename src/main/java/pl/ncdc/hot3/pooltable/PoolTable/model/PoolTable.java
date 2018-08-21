package pl.ncdc.hot3.pooltable.PoolTable.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * PoolTable class
 */
public class PoolTable {

    @JsonIgnore
    private List<Line> predictions;

    private ArrayList<Ball> balls;
    private Line cue;
    private byte[] tableImage;
    private Line targetLine;


    public PoolTable(){}

//    /**
//     * PoolTable constructor
//     *
//     * @param balls Array of Balls
//     * @param cue Cue
//     */
//    public PoolTable(ArrayList<Ball> balls, Line cue) {
//        this.balls = balls;
//        this.cue = cue;
//    }


    public List<Line> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Line> predictions) {
        this.predictions = predictions;
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
    public Line getCue() {
        return cue;
    }

    /**
     * Set Cue
     *
     * @param cue Cue
     */
    public void setCue(Line cue) {
        this.cue = cue;
    }

    public byte[] getTableImage() {
        return tableImage;
    }

    public void setTableImage(byte[] tableImage) {
        this.tableImage = tableImage;
    }

    public Line getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(Line targetLine) {
        this.targetLine = targetLine;
    }
}
