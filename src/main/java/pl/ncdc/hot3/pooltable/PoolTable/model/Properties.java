package pl.ncdc.hot3.pooltable.PoolTable.model;

import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

/**
 * Properties class
 */

@Component
public class Properties {

    public final String BASE_PATH = this.getClass().getClassLoader().getResource("emptyTable.png").toString();
    public final String TESTS_PATH = "src/main/resources/";

    // Bands
    private double tableBandLeft;
    private double tableBandRight;
    private double tableBandTop;
    private double tableBandBottom;

    // Pocket
    private double tablePocketMinRadius;
    private double tablePocketMaxRadius;
    private double tablePocketMinDistance;

    // Ball
    private int ballMaxRadius;
    private int ballMinRadius;
    private int ballMinDistance;
    private Scalar solidDrawColor;
    private Scalar stripedDrawColor;

    // Canny
    private double cannyHighThreshold;
    private double cannyRatio;

    // Image
    private double imageSourceWidth;
    private double imageSourceHeight;

    // Stick
    private double cueThickness;
    private double parallelTolerance;
    private double previousFramesMoveTolerance;
    private int cueDetectDelay;

    // Predictions
    private int predictionDepth;

    // Replays
    private boolean showPreviousPosition;
    private int previousFramesFrequency;

    public double getPreviousFramesMoveTolerance() {
        return previousFramesMoveTolerance;
    }

    public void setPreviousFramesMoveTolerance(double previousFramesMoveTolerance) {
        this.previousFramesMoveTolerance = previousFramesMoveTolerance;
    }

    public Scalar getSolidDrawColor() {
        return solidDrawColor;
    }

    public void setSolidDrawColor(Scalar solidDrawColor) {
        this.solidDrawColor = solidDrawColor;
    }

    public Scalar getStripedDrawColor() {
        return stripedDrawColor;
    }

    public void setStripedDrawColor(Scalar stripedDrawColor) {
        this.stripedDrawColor = stripedDrawColor;
    }

    public Properties() {
        this.tableBandLeft = 165;
        this.tableBandRight = 1948;
        this.tableBandTop = 350;
        this.tableBandBottom = 1236;
        this.predictionDepth = 1;

        this.cueThickness = 60;
        this.parallelTolerance = 0.15;
        this.previousFramesMoveTolerance = 10;
        this.cueDetectDelay = 4;

        this.ballMaxRadius = 22;
        this.ballMinRadius = 16;
        this.ballMinDistance = 36;
        this.solidDrawColor = new Scalar(0, 0, 255);
        this.stripedDrawColor = new Scalar(0, 255, 0);

        this.showPreviousPosition = true;
        this.previousFramesFrequency = 4;
    }

    /**
     * Properties constructor
     *
     * @param tableBandLeft
     * @param tableBandRight
     * @param tableBandTop
     * @param tableBandBottom
     * @param tablePocketMinRadius
     * @param tablePocketMaxRadius
     * @param tablePocketMinDistance
     * @param ballMaxRadius
     * @param ballMinRadius
     * @param ballMinDistance
     * @param cannyHighThreshold
     * @param cannyRatio
     * @param imageSourceWidth
     * @param imageSourceHeight
     * @param predictionDepth
     */
    public Properties(double tableBandLeft,
                      double tableBandRight,
                      double tableBandTop,
                      double tableBandBottom,
                      double tablePocketMinRadius,
                      double tablePocketMaxRadius,
                      double tablePocketMinDistance,
                      double ballMaxRadius,
                      double ballMinRadius,
                      double ballMinDistance,
                      double cannyHighThreshold,
                      double cannyRatio,
                      double imageSourceWidth,
                      double imageSourceHeight,
                      int predictionDepth
    ) {}

    public boolean isShowPreviousPosition() {
        return showPreviousPosition;
    }

    public void setShowPreviousPosition(boolean showPreviousPosition) {
        this.showPreviousPosition = showPreviousPosition;
    }

    public int getPreviousFramesFrequency() {
        return previousFramesFrequency;
    }

    public void setPreviousFramesFrequency(int previousFramesFrequency) {
        this.previousFramesFrequency = previousFramesFrequency;
    }

    public double getCueThickness() {
        return cueThickness;
    }

    public void setCueThickness(double cueThickness) {
        this.cueThickness = cueThickness;
    }

    public double getParallelTolerance() {
        return parallelTolerance;
    }

    public void setParallelTolerance(double parallelTolerance) {
        this.parallelTolerance = parallelTolerance;
    }

    /**
     * Get table band left
     *
     * @return
     */
    public double getTableBandLeft() {
        return tableBandLeft;
    }

    /**
     * Set table band left
     *
     * @param tableBandLeft
     */
    public void setTableBandLeft(double tableBandLeft) {
        this.tableBandLeft = tableBandLeft;
    }

    /**
     * Get table band right
     *
     * @return
     */
    public double getTableBandRight() {
        return tableBandRight;
    }

    /**
     * Set table band right
     *
     * @param tableBandRight
     */
    public void setTableBandRight(double tableBandRight) {
        this.tableBandRight = tableBandRight;
    }

    /**
     * Get table band top
     *
     * @return
     */
    public double getTableBandTop() {
        return tableBandTop;
    }

    /**
     * Set table band top
     *
     * @param tableBandTop
     */
    public void setTableBandTop(double tableBandTop) {
        this.tableBandTop = tableBandTop;
    }

    /**
     * Get table band bottom
     *
     * @return
     */
    public double getTableBandBottom() {
        return tableBandBottom;
    }

    /**
     * Set table band bottom
     *
     * @param tableBandBottom
     */
    public void setTableBandBottom(double tableBandBottom) {
        this.tableBandBottom = tableBandBottom;
    }

    /**
     * Get table pocket min radius
     *
     * @return
     */
    public double getTablePocketMinRadius() {
        return tablePocketMinRadius;
    }

    /**
     * Set table pocket min radius
     *
     * @param tablePocketMinRadius
     */
    public void setTablePocketMinRadius(double tablePocketMinRadius) {
        this.tablePocketMinRadius = tablePocketMinRadius;
    }

    /**
     * Get table pocket max radius
     *
     * @return
     */
    public double getTablePocketMaxRadius() {
        return tablePocketMaxRadius;
    }

    /**
     * Set table pocket max radius
     *
     * @param tablePocketMaxRadius
     */
    public void setTablePocketMaxRadius(double tablePocketMaxRadius) {
        this.tablePocketMaxRadius = tablePocketMaxRadius;
    }

    /**
     * Get table pocket min distance
     *
     * @return
     */
    public double getTablePocketMinDistance() {
        return tablePocketMinDistance;
    }

    /**
     * Set table pocket min distance
     *
     * @param tablePocketMinDistance
     */
    public void setTablePocketMinDistance(double tablePocketMinDistance) {
        this.tablePocketMinDistance = tablePocketMinDistance;
    }

    /**
     * Get ball max radius
     *
     * @return
     */
    public int getBallMaxRadius() {
        return ballMaxRadius;
    }

    /**
     * Set ball max radius
     *
     * @param ballMaxRadius
     */
    public void setBallMaxRadius(int ballMaxRadius) {
        this.ballMaxRadius = ballMaxRadius;
    }

    /**
     * Get ball min radius
     *
     * @return
     */
    public int getBallMinRadius() {
        return ballMinRadius;
    }

    /**
     * Set ball min radius
     *
     * @param ballMinRadius
     */
    public void setBallMinRadius(int ballMinRadius) {
        this.ballMinRadius = ballMinRadius;
    }

    /**
     * Get ball min distance
     *
     * @return
     */
    public int getBallMinDistance() {
        return ballMinDistance;
    }

    /**
     * Set ball min distance
     *
     * @param ballMinDistance
     */
    public void setBallMinDistance(int ballMinDistance) {
        this.ballMinDistance = ballMinDistance;
    }

    /**
     * Get canny high threshold
     *
     * @return
     */
    public double getCannyHighThreshold() {
        return cannyHighThreshold;
    }

    /**
     * Set canny high threshold
     *
     * @param cannyHighThreshold
     */
    public void setCannyHighThreshold(double cannyHighThreshold) {
        this.cannyHighThreshold = cannyHighThreshold;
    }

    /**
     * Get canny ratio
     *
     * @return
     */
    public double getCannyRatio() {
        return cannyRatio;
    }

    /**
     * Set canny ratio
     *
     * @param cannyRatio
     */
    public void setCannyRatio(double cannyRatio) {
        this.cannyRatio = cannyRatio;
    }

    /**
     * Get image source width
     *
     * @return
     */
    public double getImageSourceWidth() {
        return imageSourceWidth;
    }

    /**
     * Set image source width
     *
     * @param imageSourceWidth
     */
    public void setImageSourceWidth(double imageSourceWidth) {
        this.imageSourceWidth = imageSourceWidth;
    }

    /**
     * Get image source height
     *
     * @return
     */
    public double getImageSourceHeight() {
        return imageSourceHeight;
    }

    /**
     * Set image source height
     *
     * @param imageSourceHeight
     */
    public void setImageSourceHeight(double imageSourceHeight) {
        this.imageSourceHeight = imageSourceHeight;
    }

    public int getPredictionDepth() {
        return predictionDepth;
    }

    public void setPredictionDepth(int predictionDepth) {
        this.predictionDepth = predictionDepth;
    }

    public int getCueDetectDelay() {
        return cueDetectDelay;
    }

    public void setCueDetectDelay(int cueDetectDelay) {
        this.cueDetectDelay = cueDetectDelay;
    }

    public boolean isPointInsideBand(Point point){
        if (point.x >= this.getTableBandLeft() - 5 && point.x <= this.getTableBandRight() + 5) {
            if (point.y >= this.getTableBandTop() - 5 && point.y <= this.getTableBandBottom() + 5) {
                return true;
            }
        }
        return false;
    }

    public String getFullPath(String filename) throws FileNotFoundException {
        String path = this.getClass().getClassLoader().getResource(filename).getPath().replaceAll("%20"," ").substring(1);
        return path;
    }
}
