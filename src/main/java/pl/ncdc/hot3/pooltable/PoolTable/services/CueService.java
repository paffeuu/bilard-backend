package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;
import java.util.List;

import static pl.ncdc.hot3.pooltable.PoolTable.services.LineService.calculateDistanceBetweenPoints;

@Service
public class CueService {

    final static Logger LOGGER = LoggerFactory.getLogger(CueService.class);

    private Properties properties;
    private LineService lineService;
    private BandsService bandsService;

    private Line[] prevCueLines;
    private Line previousAverageLine;
    private int cueDetectDelay, detectedCueCounter;

    private Point[] targetEnds;
    private int targetEndCurrentIndex;
    private List<Line> targetLines;
    private Line[] targetField;
    private int indexOfTargetLine;

    public Point debugCloserToWhite;
    public Point debugFurtherToWhite;

    @Autowired
    public CueService(
            Properties properties,
            LineService lineService,
            BandsService bandsService
    ){
        this.properties = properties;
        this.lineService = lineService;
        this.bandsService = bandsService;
        this.detectedCueCounter = 0;

        cueDetectDelay = properties.getCueDetectDelay();
        prevCueLines = new Line[cueDetectDelay];

        this.targetEnds = new Point[properties.getTargetLineStabilizeCount()];
        this.targetEndCurrentIndex = 0;
        this.previousAverageLine = null;

        this.targetLines = new ArrayList<>(properties.getCountOfTargetLines());
        this.targetField = new Line[2];
        this.indexOfTargetLine = 0;
    }

    private double calcAbsoluteDistance(double value1, double value2){
        return Math.abs(value1 - value2);
    }

    /**
     * It's giving a new line (predicted) for argument line.
     *
     * @param line Line for calculate predict
     *
     * @return Predicted line.
     *
     * @throws CueServiceException  When bumping point is out of bands (bad calibration).
     * @throws LineServiceException When cannot extend new line.
     */
    public Line predictTrajectoryAfterBump(Line line) throws CueServiceException, LineServiceException {
        Point bumpPoint = line.getEnd();
        Point halfDistance;

        if (Math.abs(properties.getTableBandLeft() - bumpPoint.x) <= properties.getBumpPointDelta()) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (Math.abs(properties.getTableBandRight() - bumpPoint.x) <= properties.getBumpPointDelta()) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (Math.abs(properties.getTableBandTop() - bumpPoint.y) <= properties.getBumpPointDelta()) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (Math.abs(properties.getTableBandBottom() - bumpPoint.y) <= properties.getBumpPointDelta()) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else {
            throw new CueServiceException("Cannot find predicted line. Bump point out of bands!");
        }

        double distanceX = halfDistance.x - line.getBegin().x;
        double distanceY = halfDistance.y - line.getBegin().y;

        Line predictedLine = new Line(
                bumpPoint,
                new Point(halfDistance.x + distanceX, halfDistance.y + distanceY)

        );

        try {
            predictedLine = lineService.getExtendedStickLineForOneSide(predictedLine);
        }catch (ExtendLineException e) {
            LOGGER.warn("Cannot extend predicted line. \n" + predictedLine + ". Nested: " + e);
        }

        return predictedLine;
    }

    public Line findStickLine(List<Line> innerLines) throws MissingCueLineException {

        Line cueLine = null;

        if (innerLines.isEmpty()) {
            return cueLine;
        } else if (innerLines.size() == 1) {
            return innerLines.get(0);
        }

        double pMin = properties.getParallelTolerance();
        double distMin = properties.getMinBCoordinateForLines();
        double[] ABCCoordinatesLine1 = new double[3];
        double[] ABCCoordinatesLine2 = new double[3];

        int indexOfLine_A = 0, indexOfLine_B = 0;

        for (int i = 0; i < innerLines.size() - 1; i++){
            for (int j = 0; j < innerLines.size(); j++){
                if (i != j) {
                    ABCCoordinatesLine1 = calcAllCoordinate(innerLines.get(i));
                    ABCCoordinatesLine2 = calcAllCoordinate(innerLines.get(j));

                    if (ABCCoordinatesLine1[1] != 0 && ABCCoordinatesLine2[1] != 0){
                        double a1 = ABCCoordinatesLine1[0] / ABCCoordinatesLine1[1] * -1;
                        double a2 = ABCCoordinatesLine2[0] / ABCCoordinatesLine2[1] * -1;

                        double b1 = ABCCoordinatesLine1[2] / ABCCoordinatesLine1[1] * -1;
                        double b2 = ABCCoordinatesLine2[2] / ABCCoordinatesLine2[1] * -1;

                        if ( Math.abs(a1) >= 20 || Math.abs(a2) >= 20) {
                            pMin = 1000;
                        }

                        if (Math.abs(a1 - a2) < pMin && Math.abs(b1 - b2) >= distMin) {
                            pMin = Math.abs(a1 - a2);
                            indexOfLine_A = i;
                            indexOfLine_B = j;
                        }

                    }
                }
            }
        }

        Line newLineBetweenShort = null;
        if (indexOfLine_A != indexOfLine_B) {
            double X1 = (innerLines.get(indexOfLine_A).getBegin().x + innerLines.get(indexOfLine_B).getBegin().x) / 2;
            double X2 = (innerLines.get(indexOfLine_A).getEnd().x + innerLines.get(indexOfLine_B).getEnd().x) / 2;
            double Y1 = (innerLines.get(indexOfLine_A).getBegin().y + innerLines.get(indexOfLine_B).getBegin().y) / 2;
            double Y2 = (innerLines.get(indexOfLine_A).getEnd().y + innerLines.get(indexOfLine_B).getEnd().y) / 2;
            Point newBegin = new Point(X1, Y1);
            Point newEnd = new Point(X2, Y2);
            newLineBetweenShort = new Line(newBegin, newEnd);
        }

        return newLineBetweenShort;
    }

    public Line directAndExtend(Line line, Point whiteBall) {
        Line newLineBetweenLong = line;

        double beginDist = getDistanceBetweenPoints(line.getBegin(), whiteBall);
        double endDist = getDistanceBetweenPoints(line.getEnd(), whiteBall);

        if (beginDist <=  endDist) {
            LineService.switchPoints(newLineBetweenLong);
        }
        debugCloserToWhite = newLineBetweenLong.getEnd();
        debugFurtherToWhite = newLineBetweenLong.getBegin();

        try {
            newLineBetweenLong = lineService.getExtendedStickLineForOneSide(newLineBetweenLong);
        } catch (LineServiceException e) {
            LOGGER.warn("Cannot extend it: " + line);
        }

        return newLineBetweenLong;
    }

    public Line stabilizeWithPrevious(Line cueLine) {
        Line stabileCueLine = cueLine;
        double distMin = properties.getPreviousFramesMoveTolerance();

        prevCueLines[detectedCueCounter] = cueLine;
        if (detectedCueCounter++ > 0){
            detectedCueCounter = detectedCueCounter % cueDetectDelay;

            double[] prevSumXs = { 0, 0 }, prevSumYs = { 0, 0 };
            int prevLinesCounter = 0;
            for (int i = 0; i < (cueDetectDelay); i++){
                int tempIdx = (detectedCueCounter + i) % properties.getCueDetectDelay();

                if (prevCueLines[tempIdx] != null){
                    if (LineService.calculateDistanceBetweenPoints(prevCueLines[tempIdx].getBegin(), cueLine.getBegin()) <= distMin &&
                            LineService.calculateDistanceBetweenPoints(prevCueLines[tempIdx].getEnd(), cueLine.getEnd()) <= distMin) {
                        prevLinesCounter++;
                        prevSumXs[0] += prevCueLines[tempIdx].getBegin().x;
                        prevSumYs[0] += prevCueLines[tempIdx].getBegin().y;
                        prevSumXs[1] += prevCueLines[tempIdx].getEnd().x;
                        prevSumYs[1] += prevCueLines[tempIdx].getEnd().y;
                    }
                }
            }

            if (prevLinesCounter > 24) {
                Point averageBegin = new Point(prevSumXs[0] / prevLinesCounter, prevSumYs[0] / prevLinesCounter);
                Point averageEnd = new Point(prevSumXs[1] / prevLinesCounter, prevSumYs[1] / prevLinesCounter);

                Point newBegin = new Point((averageBegin.x + cueLine.getBegin().x) / 2, (averageBegin.y + cueLine.getBegin().y) / 2);
                Point newEnd = new Point((averageEnd.x + cueLine.getEnd().x) / 2, (averageEnd.y + cueLine.getEnd().y) / 2);


                stabileCueLine.setBegin(newBegin);
                stabileCueLine.setEnd(newEnd);

                previousAverageLine = stabileCueLine;
            }
        }

        return stabileCueLine;
    }

    public static double getDistanceBetweenPoints(Point point1, Point point2) {
        return Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2));
    }

    public double[] calcAllCoordinate(Line line) {
        double Y = line.getBegin().y - line.getEnd().y;
        double X = line.getBegin().x - line.getEnd().x;

        double a = Y / (X == 0 ? 0.1 : X);
        double b = line.getBegin().y - line.getBegin().x * a;
        return new double[]{a, -1, b};
    }

    /**
     * Calculate distance between point and line
     *
     * @param point point
     * @param line  line
     *
     * @return distance
     */
    public double calculateDistanceBetweenPointAndLine(Point point, Line line) {
        double[] coordinates = calcAllCoordinate(line);

        return Math.abs(coordinates[0] * point.x + coordinates[1] * point.y + coordinates[2]) /
                Math.sqrt(Math.pow(coordinates[0], 2) + Math.pow(coordinates[1], 2));
    }

    /**
     * Magic method which calculate two points on aiming line based on distance from ball center. Ghost ball definition
     * - http://www.easypooltutor.com/img/lessons/ghost_ball.png
     *
     * @param line aiming line
     * @param ball collision ball
     *
     * @return collision line based on object ball and ghost ball
     *
     * @throws LineServiceException if can not get extended cue line for one side
     */
    public Line findBallCollisionLine(Line line, Ball ball) throws LineServiceException {
        double[] coordinates = calcAllCoordinate(line);
        double A = coordinates[0];
        double B = coordinates[1];
        double C = coordinates[2];
        double Sx = ball.getX();
        double Sy = ball.getY();
        double d = ball.getRadius() * 2;

        double a = (B * B) + (A * A);
        double b = (-2 * Sx * B * B) + (2 * C * A) + (2 * B * Sy * A);
        double c = (Sx * Sx * B * B) + (C * C) + (2 * B * Sy * C) + (Sy * Sy * B * B) - (B * B * d * d);

        double delta = b * b - 4 * a * c;

        if (0 > delta) {
            return null;
        }

        double x1 = (-b - Math.sqrt(delta)) / (2 * a);
        double x2 = (-b + Math.sqrt(delta)) / (2 * a);
        double y1 = (-C - A * x1) / B;
        double y2 = (-C - A * x2) / B;

        double distance1 = calculateDistanceBetweenPoints(new Point(x1, y1), line.getEnd());
        double distance2 = calculateDistanceBetweenPoints(new Point(x2, y2), line.getEnd());
        Point point = new Point();

        if (distance1 >= distance2) {
            point.x = x1;
            point.y = y1;
        } else {
            point.x = x2;
            point.y = y2;
        }

        Line targetLine = lineService.getExtendedStickLineForOneSide(new Line(
                point,
                new Point(
                        ball.getX(),
                        ball.getY()
                )
        ));

        stabilizeTargetLine(targetLine);


        if (!targetLines.isEmpty() ||
                targetLines.size() < properties.getCountOfTargetLines()){
            targetLines.add(targetLine);
        } else {
            targetLines.clear();
            targetLines.set(indexOfTargetLine, targetLine);
        }
        indexOfTargetLine = (indexOfTargetLine + 1) % properties.getCountOfTargetLines();

        return targetLine;
    }

    public void stabilizeTargetLine(Line targetLine){
        targetEnds[targetEndCurrentIndex++] = targetLine.getEnd();
        targetEndCurrentIndex = targetEndCurrentIndex % properties.getTargetLineStabilizeCount();

        double sumOfXsApproved = 0;
        double sumOfYsApproved = 0;
        int approvedCounter = 0;

        double allSumOfXs = 0;
        double allSumOfYs = 0;


        for (int i = 0; i < properties.getTargetLineStabilizeCount() - 1; i++){
            int tempIndex = (targetEndCurrentIndex + 1) % properties.getTargetLineStabilizeCount();

            if (targetEnds[tempIndex] != null) {
                if (LineService.getDistanceBetweenPoints(targetLine.getEnd(), targetEnds[tempIndex]) <= properties.getTargetEndMoveTolerance()) {
                    approvedCounter++;
                    sumOfXsApproved += targetEnds[tempIndex].x;
                    sumOfYsApproved += targetEnds[tempIndex].y;
                }

                allSumOfXs += targetEnds[tempIndex].x;
                allSumOfYs += targetEnds[tempIndex].y;

            }
        }

        if (approvedCounter >= properties.getTargetLineStabilizeCount() / 2) {
            targetLine.setEnd(new Point(sumOfXsApproved / approvedCounter, sumOfYsApproved / approvedCounter));
        } else {
            targetLine.setEnd(new Point(allSumOfXs / (properties.getTargetLineStabilizeCount() - 1), allSumOfYs / (properties.getTargetLineStabilizeCount() - 1)));
        }
    }

    public Line getPreviousAverageLine() {
        return previousAverageLine;
    }

    public List<Line> getTargetLines() {
        return targetLines;
    }

    private Point getTargetFieldCenter(){
        Point center = null;

        if (!targetLines.isEmpty()){
            double sumXs = 0;
            double sumYs = 0;
            for (Line line : targetLines) {
                sumXs += line.getEnd().x;
                sumYs += line.getEnd().y;
            }
            center = new Point(sumXs / targetLines.size(), sumYs / targetLines.size());
        }

        return center;
    }

    private Line[] getTunnel() {
        Point centre = getTargetFieldCenter();

        for ()
        LineService.getDistanceBetweenPoints()

    }

}
