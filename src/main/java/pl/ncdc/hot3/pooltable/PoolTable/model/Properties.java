package pl.ncdc.hot3.pooltable.PoolTable.model;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.springframework.stereotype.Component;

/**
 * Properties class
 */

@Component
public class Properties {

    private boolean isDebugActive;

    // Bands
    private double tableBandLeft;
    private double tableBandRight;
    private double tableBandTop;
    private double tableBandBottom;

    // Pocket
    private int tablePocketRadius;
    private double tablePocketMinRadius;
    private double tablePocketMaxRadius;
    private double tablePocketMinDistance;
    private Point leftTopPocketPoint;
    private Point rightTopPocketPoint;
    private Point leftBotPocketPoint;
    private Point rightBotPocketPoint;
    private Point midTopPocketPoint;
    private Point midBotPocketPoint;

    // Ball parameters
    private int ballExpectedRadius;
    private int ballMaxRadius;
    private int ballMinRadius;
    private int ballMinDistance;

    // Ball img processing parameters
    private int houghCirclesParam1;
    private int houghCirclesParam2;
    private int whitePixelsPercentageBorder;

    // Ball IDs
    private int whiteBallId;
    private int blackBallId;
    private int firstSolidBallId;
    private int firstStripedBallId;

    // Ball drawing parameters
    private int ballThickness;

    // Ball correction counter
    private final int prevBallsCorrectorCount;
    private final int previousBallsPositionsToCompare;

    // Canny
    private double cannyHighThreshold;
    private double cannyRatio;

    // Image
    private double imageSourceWidth;
    private double imageSourceHeight;

    // Stick
    private double cueThickness;
    private double parallelTolerance;
    private double minBCoordinateForLines;
    private double previousFramesMoveTolerance;
    private int cueDetectDelay;
    private int cueStickLineThickness;

    // Predictions
    private int predictionDepth;
    private int bumpPointDelta;

    // Replays
    private boolean showPreviousPosition;
    private int previousFramesFrequency;


    //Camera prameters
    private static String cameraUrl = "rtsp://hot:kamerabilardowa@192.168.253.214:554/Streaming/Channels/1?transportmode=unicast&profile=Profile_1";

    private Mat cameraMatrix = Mat.zeros(3, 3, CvType.CV_64F);
    private Mat distCoeffs = Mat.zeros(8, 1, CvType.CV_64F);

    //Library import paths
    private static String windowsOpencvPath = System.getProperty("user.dir") + "\\lib\\" + Core.NATIVE_LIBRARY_NAME + ".dll";
    private static String windowsFfmpegPath = System.getProperty("user.dir") + "\\lib\\" + "opencv_ffmpeg342_64.dll";
    private static String linuxOpencvPath = "/usr/local/share/OpenCV/java/" + "libopencv_java342" + ".so";


    public Properties() {
        this.isDebugActive = true;

        this.ballMaxRadius = 22;
        this.ballMinRadius = 16;
        this.ballMinDistance = 36;
        this.ballExpectedRadius = 19;
        this.houghCirclesParam1 = 30;
        this.houghCirclesParam2 = 15;
        this.whiteBallId = 0;
        this.blackBallId = 8;
        this.firstSolidBallId = 10;
        this.firstStripedBallId = 30;
        this.ballThickness = 5;
        this.whitePixelsPercentageBorder = 16;
        this.prevBallsCorrectorCount = 12;
        this.previousBallsPositionsToCompare = 12;

        this.tableBandLeft = 147;
        this.tableBandRight = 1130;
        this.tableBandTop = 131;
        this.tableBandBottom = 628;
        this.tablePocketRadius = 45;

        this.predictionDepth = 1;
        this.bumpPointDelta = 2;

        this.cueThickness = 60;
        this.parallelTolerance = 0.15;
        this.minBCoordinateForLines = 5;
        this.previousFramesMoveTolerance = 60;
        this.cueDetectDelay = 32;
        this.cueStickLineThickness = 8;

        this.leftTopPocketPoint = new Point(tableBandLeft - 10, tableBandTop - 10);
        this.rightTopPocketPoint = new Point(tableBandRight, tableBandTop);
        this.leftBotPocketPoint = new Point(tableBandLeft - 10, tableBandBottom + 10);
        this.rightBotPocketPoint = new Point(tableBandRight + 10, tableBandBottom + 10);
        this.midTopPocketPoint = new Point(((tableBandLeft + tableBandRight) / 2) + 18, tableBandTop - 25);
        this.midBotPocketPoint = new Point(((tableBandLeft + tableBandRight) / 2) + 18, tableBandBottom + 25);

        this.showPreviousPosition = true;
        this.previousFramesFrequency = 4;

        cameraMatrix.put(2, 2, 1);
        cameraMatrix.put(0, 0, 991.4262945972393);
        cameraMatrix.put(0, 2, 640);
        cameraMatrix.put(1, 1, 993.9357197471496);
        cameraMatrix.put(1, 2, 360);

        distCoeffs.put(0, 0, -0.4110309525718729);
        distCoeffs.put(1, 0, 0.2250083648489881);
    }

    public static String getWindowsOpencvPath() {
        return windowsOpencvPath;
    }

    public static String getWindowsFfmpegPath() {
        return windowsFfmpegPath;
    }

    public static String getLinuxOpencvPath() {
        return linuxOpencvPath;
    }

    public int getWhitePixelsPercentageBorder() {
        return whitePixelsPercentageBorder;
    }

    public int getWhiteBallId() {
        return whiteBallId;
    }

    public int getBlackBallId() {
        return blackBallId;
    }

    public int getFirstSolidBallId() {
        return firstSolidBallId;
    }

    public int getFirstStripedBallId() {
        return firstStripedBallId;
    }

    public int getBallThickness() {
        return ballThickness;
    }

    public double getPreviousFramesMoveTolerance() {
        return previousFramesMoveTolerance;
    }

    public void setPreviousFramesMoveTolerance(double previousFramesMoveTolerance) {
        this.previousFramesMoveTolerance = previousFramesMoveTolerance;
    }

    public int getCueStickLineThickness() {
        return cueStickLineThickness;
    }

    public void setCueStickLineThickness(int cueStickLineThickness) {
        this.cueStickLineThickness = cueStickLineThickness;
    }

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


    public int getHoughCirclesParam1() {
        return houghCirclesParam1;
    }

    public int getHoughCirclesParam2() {
        return houghCirclesParam2;
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

    public int getTablePocketRadius() {
        return tablePocketRadius;
    }

    public void setTablePocketRadius(int tablePocketRadius) {
        this.tablePocketRadius = tablePocketRadius;
    }

    public Point getLeftTopPocketPoint() {
        return leftTopPocketPoint;
    }

    public void setLeftTopPocketPoint(Point leftTopPocketPoint) {
        this.leftTopPocketPoint = leftTopPocketPoint;
    }

    public Point getRightTopPocketPoint() {
        return rightTopPocketPoint;
    }

    public void setRightTopPocketPoint(Point rightTopPocketPoint) {
        this.rightTopPocketPoint = rightTopPocketPoint;
    }

    public Point getLeftBotPocketPoint() {
        return leftBotPocketPoint;
    }

    public void setLeftBotPocketPoint(Point leftBotPocketPoint) {
        this.leftBotPocketPoint = leftBotPocketPoint;
    }

    public Point getRightBotPocketPoint() {
        return rightBotPocketPoint;
    }

    public void setRightBotPocketPoint(Point rightBotPocketPoint) {
        this.rightBotPocketPoint = rightBotPocketPoint;
    }

    public Point getMidTopPocketPoint() {
        return midTopPocketPoint;
    }

    public void setMidTopPocketPoint(Point midTopPocketPoint) {
        this.midTopPocketPoint = midTopPocketPoint;
    }

    public Point getMidBotPocketPoint() {
        return midBotPocketPoint;
    }

    public void setMidBotPocketPoint(Point midBotPocketPoint) {
        this.midBotPocketPoint = midBotPocketPoint;
    }


    /**
     * Get ball expected radius
     *
     * @return ball radius
     */
    public int getBallExpectedRadius() {
        return ballExpectedRadius;
    }

    /**
     * Set ball expected radius
     *
     * @param ballExpectedRadius ball expected radius
     */
    public void setBallExpectedRadius(int ballExpectedRadius) {
        this.ballExpectedRadius = ballExpectedRadius;
    }

    public final int getPreviousBallsPositionsToCompare() {
        return previousBallsPositionsToCompare;
    }

    public double getMinBCoordinateForLines() {
        return minBCoordinateForLines;
    }

    public void setMinBCoordinateForLines(double minBCoordinateForLines) {
        this.minBCoordinateForLines = minBCoordinateForLines;
    }

    public static String getCameraUrl() {
        return cameraUrl;
    }

    public Mat getCameraMatrix() {
        return this.cameraMatrix;
    }

    public Mat getDistCoeffs() {
        return this.distCoeffs;
    }
    /**
     * Get bump point delta
     *
     * @return
     */
    public int getBumpPointDelta() {
        return bumpPointDelta;
    }

    /**
     * Set bump point delta
     *
     * @param bumpPointDelta
     */
    public void setBumpPointDelta(int bumpPointDelta) {
        this.bumpPointDelta = bumpPointDelta;
    }
    public boolean isDebugActive() {
        return isDebugActive;
    }

    public void setDebugActive(boolean debugActive) {
        isDebugActive = debugActive;
    }
}
