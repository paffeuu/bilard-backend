package pl.ncdc.hot3.pooltable.PoolTable.model;

/**
 * Properties class
 */
public class Properties {
    // Band
    private double tableBandLeft;
    private double tableBandRight;
    private double tableBandTop;
    private double tableBandBottom;

    // Pocket
    private double tablePocketMinRadius;
    private double tablePocketMaxRadius;
    private double tablePocketMinDistance;

    // Ball
    private double ballMaxRadius;
    private double ballMinRadius;
    private double ballMinDistance;

    // Canny
    private double cannyHighThreshold;
    private double cannyRatio;

    // Image
    private double imageSourceWidth;
    private double imageSourceHeight;

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
                      double imageSourceHeight) {

        this.tableBandLeft = tableBandLeft;
        this.tableBandRight = tableBandRight;
        this.tableBandTop = tableBandTop;
        this.tableBandBottom = tableBandBottom;
        this.tablePocketMinRadius = tablePocketMinRadius;
        this.tablePocketMaxRadius = tablePocketMaxRadius;
        this.tablePocketMinDistance = tablePocketMinDistance;
        this.ballMaxRadius = ballMaxRadius;
        this.ballMinRadius = ballMinRadius;
        this.ballMinDistance = ballMinDistance;
        this.cannyHighThreshold = cannyHighThreshold;
        this.cannyRatio = cannyRatio;
        this.imageSourceWidth = imageSourceWidth;
        this.imageSourceHeight = imageSourceHeight;
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
    public double getBallMaxRadius() {
        return ballMaxRadius;
    }

    /**
     * Set ball max radius
     *
     * @param ballMaxRadius
     */
    public void setBallMaxRadius(double ballMaxRadius) {
        this.ballMaxRadius = ballMaxRadius;
    }

    /**
     * Get ball min radius
     *
     * @return
     */
    public double getBallMinRadius() {
        return ballMinRadius;
    }

    /**
     * Set ball min radius
     *
     * @param ballMinRadius
     */
    public void setBallMinRadius(double ballMinRadius) {
        this.ballMinRadius = ballMinRadius;
    }

    /**
     * Get ball min distance
     *
     * @return
     */
    public double getBallMinDistance() {
        return ballMinDistance;
    }

    /**
     * Set ball min distance
     *
     * @param ballMinDistance
     */
    public void setBallMinDistance(double ballMinDistance) {
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
}
