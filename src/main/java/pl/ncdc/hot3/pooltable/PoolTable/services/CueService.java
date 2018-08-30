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

import java.util.List;

import static pl.ncdc.hot3.pooltable.PoolTable.services.LineService.calculateDistanceBetweenPoints;

@Service
public class CueService {

    final static Logger LOGGER = LoggerFactory.getLogger(CueService.class);

    private Properties properties;
    private int counter = 0;
    private LineService lineService;

    private Line[] prevCueLines;
    private Line previousAverageLine;
    private int cueDetectDelay, detectedCueCounter;

    private Point[] targetEnds;
    private int targetEndCurrentIndex;

    private ArrayList<Line> previousCues = new ArrayList<Line>(32);
    private ArrayList<Line> detectionOutOfScope = new ArrayList<Line>();
    private int frameCounter = 0;
    public Point debugCloserToWhite;
    public Point debugFurtherToWhite;



    @Autowired
    public CueService(
            Properties properties,
            LineService lineService
    ){
        this.properties = properties;
        this.lineService = lineService;
        this.detectedCueCounter = 0;

        cueDetectDelay = properties.getCueDetectDelay();
        prevCueLines = new Line[cueDetectDelay];

        this.targetEnds = new Point[properties.getTargetLineStabilizeCount()];
        this.targetEndCurrentIndex = 0;
        previousAverageLine = null;
    }

    /**
     * Returns prediction line after bump
     *
     *  aiming line
     *
     * @return prediction line
     *
     * @throws CueServiceException  if bump point is out of band
     * @throws LineServiceException if can not extend cue line for one side
     */
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
        double bestPararell = 0;
        double distMin = properties.getMinBCoordinateForLines();
        double[] ABCCoordinatesLine1 = new double[3];
        double[] ABCCoordinatesLine2 = new double[3];

        int indexOfLine_A = 0, indexOfLine_B = 0;

        for (int i = 0; i < innerLines.size() - 1; i++){
            ABCCoordinatesLine1 = calcAllCoordinate(innerLines.get(i));
            for (int j = 0; j < innerLines.size(); j++){
                if (i != j) {
                    ABCCoordinatesLine2 = calcAllCoordinate(innerLines.get(j));
                        double a1 = -ABCCoordinatesLine1[0];
                        double a2 = -ABCCoordinatesLine2[0];

                        double b1 = -ABCCoordinatesLine1[2];
                        double b2 = -ABCCoordinatesLine2[2];

                        if ( Math.abs(a1) >= 20 || Math.abs(a2) >= 20) {
                            pMin = 1000;
                        } else {
                            pMin = properties.getParallelTolerance();
                        }
                        if (Math.abs(a1 - a2) < pMin && Math.abs(b1 - b2) >= distMin && Math.abs(b1-b2) <= 15) {
                            pMin = Math.abs(a1 - a2);
                            indexOfLine_A = i;
                            indexOfLine_B = j;
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
            newLineBetweenLong = LineService.switchPoints(newLineBetweenLong);
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
        if (cueLine == null) {
            return null;
        }
        double[] stabilizedBeginning = { 0, 0};
        double[] stabilizedEnd = {0,0};
        double distanceTolerance = properties.getPreviousFramesMoveTolerance();

        previousCues.add(cueLine);
        if (previousCues.size() >= 20) {
            previousCues.remove(0);
        }

        for (Line line : this.previousCues) {
            if ( line != null) {
                stabilizedBeginning[0] += line.getBegin().x;
                stabilizedBeginning[1] += line.getBegin().y;
                stabilizedEnd[0] += line.getEnd().x;
                stabilizedEnd[1] += line.getEnd().y;
            }
        }










        stabilizedBeginning[0]  /= this.previousCues.size();
        stabilizedBeginning[1] /= this.previousCues.size();
        stabilizedEnd[0] /= this.previousCues.size();
        stabilizedEnd[1] /= this.previousCues.size();



        Line lin= new Line(new Point(stabilizedBeginning), new Point(stabilizedEnd));
        this.previousAverageLine = lin;
        return lin;

    }

        private Line averageOfListOfLines(List<Line> list) {
        if (list.isEmpty()) {
            return new Line(new Point(-1, -1), new Point(-1, -1));
        }
        double[] stabilizedBeginning = { 0, 0};
            double[] stabilizedEnd = {0,0};
            double distanceTolerance = properties.getPreviousFramesMoveTolerance();

            for (Line line : list) {
                if ( line != null) {
                    stabilizedBeginning[0] += line.getBegin().x;
                    stabilizedBeginning[1] += line.getBegin().y;
                    stabilizedEnd[0] += line.getEnd().x;
                    stabilizedEnd[1] += line.getEnd().y;
                }
            }
            stabilizedBeginning[0]  /= list.size();
            stabilizedBeginning[1] /= list.size();
            stabilizedEnd[0] /= list.size();
            stabilizedEnd[1] /= list.size();

            return new Line(new Point(stabilizedBeginning), new Point(stabilizedEnd));
        }

    public static double getDistanceBetweenPoints(Point point1, Point point2) {
        return Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2));
    }

    public double[] calcAllCoordinate(Line line) {
        double Y = line.getBegin().y - line.getEnd().y;
        double X = line.getBegin().x - line.getEnd().x;

        double a = Y / (X == 0 ? 0.0001 : X);
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
     * Create target line based on balls collision
     *
     * @param line aiming line
     * @param ball collision ball
     *
     * @return target line
     *
     * @throws LineServiceException if can not get extended cue line for one side
     */
    public Line findBallCollisionLine(Line line, Ball ball) throws LineServiceException {
        Point ghostBall = this.getGhostBall(line, ball);

        Line targetLine = lineService.getExtendedStickLineForOneSide(new Line(
                ghostBall,
                new Point(
                        ball.getX(),
                        ball.getY()
                )
        ));

        stabilizeTargetLine(targetLine);

        return targetLine;
    }

    /**
     * Magic method which calculate two points on aiming line based on distance from ball center and return ghost ball
     * center point. Ghost ball definition - http://www.easypooltutor.com/img/lessons/ghost_ball.png
     *
     * @param line aiming line
     * @param ball collision ball
     *
     * @return collision line based on object ball and ghost ball
     */
    public Point getGhostBall(Line line, Ball ball) {
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

        return point;
    }

    public void stabilizeTargetLine(Line targetLine){
        targetEnds[targetEndCurrentIndex++] = targetLine.getEnd();
        targetEndCurrentIndex = targetEndCurrentIndex % properties.getTargetLineStabilizeCount();

        double sumOfXs = 0;
        double sumOfYs = 0;
        int approvedCounter = 0;


        for (int i = 0; i < properties.getTargetLineStabilizeCount() - 1; i++){
            int tempIndex = (targetEndCurrentIndex + 1) % properties.getTargetLineStabilizeCount();

            if (targetEnds[tempIndex] != null) {
                if (LineService.getDistanceBetweenPoints(targetLine.getEnd(), targetEnds[tempIndex]) <= properties.getTargetEndMoveTolerance()) {
                    approvedCounter++;
                    sumOfXs += targetEnds[tempIndex].x;
                    sumOfYs += targetEnds[tempIndex].y;
                }
            }
        }

        if (approvedCounter >= properties.getTargetLineStabilizeCount() / 2) {
            targetLine.setEnd(new Point(sumOfXs / approvedCounter, sumOfYs / approvedCounter));
        }
    }

    public Line getPreviousAverageLine() {
        return previousAverageLine;
    }
}
