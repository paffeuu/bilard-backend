package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.ExtendLineException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.*;

@Service
public class LineService {
    private Properties properties;
    private BandsService bandsService;

    @Autowired
    public LineService(
            Properties properties,
            BandsService bandsService
    ) {
        this.properties = properties;
        this.bandsService = bandsService;
    }

    public Map<String, Double> getDistsToBands(Point point) {
        Map<String, Double> distances = new HashMap<>();

        distances.put("LEFT", Math.abs(point.x - properties.getTableBandLeft()));
        distances.put("RIGHT", Math.abs(point.x - properties.getTableBandRight()));
        distances.put("TOP", Math.abs(point.y - properties.getTableBandTop()));
        distances.put("BOTTOM", Math.abs(point.y - properties.getTableBandBottom()));

        return distances;
    }

    public static double calculateDistanceBetweenPoints(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /**
     * Switch points in line (begin, end)
     *
     * @param a line
     */
    public static void switchPoints(Line a) {
        Point tmp = a.getBegin();
        a.setBegin(a.getEnd());
        a.setEnd(tmp);
    }

    public Line getExtendedStickLineForOneSide(Line stickLine) throws LineServiceException {
        safeMoveLineForVertical(stickLine);

        double horizontalMove = stickLine.getEnd().x - stickLine.getBegin().x; // - < 0
        double verticalMove = stickLine.getEnd().y - stickLine.getBegin().y;   // == 0

        Line extendedLine = new Line();
        Point crosscutPoint1 = null;
        Point crosscutPoint2 = null;

        double[] ABCoordinates = LineService.calcCoordinatesAB(stickLine);
        double a = ABCoordinates[0];
        double b = ABCoordinates[1];

        if (horizontalMove >= 0 && verticalMove <= 0) {

            Point maxTop = new Point();
            maxTop.y = properties.getTableBandTop();
            maxTop.x = ((properties.getTableBandTop() - b) / a);

            Point maxRight = new Point();
            maxRight.x = properties.getTableBandRight();
            maxRight.y = properties.getTableBandRight() * a + b;

            crosscutPoint1 = maxTop;
            crosscutPoint2 = maxRight;

        } else if (horizontalMove <= 0 && verticalMove >= 0) {

            Point maxBot = new Point();
            maxBot.y = properties.getTableBandBottom();
            maxBot.x = (properties.getTableBandBottom() - b) / a;

            Point maxLeft = new Point();
            maxLeft.x = properties.getTableBandLeft();
            maxLeft.y = properties.getTableBandLeft() * a + b;

            crosscutPoint1 = maxLeft;
            crosscutPoint2 = maxBot;

        } else if (horizontalMove <= 0 && verticalMove <= 0) {

            Point maxTop = new Point();
            maxTop.y = properties.getTableBandTop();
            maxTop.x = ((properties.getTableBandTop() - b) / a);

            Point maxLeft = new Point();
            maxLeft.x = properties.getTableBandLeft();
            maxLeft.y = properties.getTableBandLeft() * a + b;

            crosscutPoint1 = maxLeft;
            crosscutPoint2 = maxTop;

        } else if (horizontalMove >= 0 && verticalMove >= 0) {

            Point maxBot = new Point();
            maxBot.y = properties.getTableBandBottom();
            maxBot.x = (properties.getTableBandBottom() - b) / a;

            Point maxRight = new Point();
            maxRight.x = properties.getTableBandRight();
            maxRight.y = properties.getTableBandRight() * a + b;

            crosscutPoint1 = maxRight;
            crosscutPoint2 = maxBot;

        }

        try {
            if (bandsService.isPointInsideBand(crosscutPoint1)) {
                extendedLine.setBegin(stickLine.getBegin());
                extendedLine.setEnd(crosscutPoint1);
            } else if (bandsService.isPointInsideBand(crosscutPoint2)) {
                extendedLine.setBegin(stickLine.getBegin());
                extendedLine.setEnd(crosscutPoint2);
            } else {
                throw new ExtendLineException("Error while trying make extended line for one side. Both crosscut points out of the bands");
            }
        } catch (NullPointerException e) {
            throw new ExtendLineException("Make extended line for one side error. Points still null value.");
        }

        return extendedLine;
    }

    public Line getExtendedStickLineForBothSides(Line stickLine) throws ExtendLineException {
        Line extendedLine = new Line();

        double[] ABCoordinates = LineService.calcCoordinatesAB(stickLine);
        double a = ABCoordinates[0];
        double b = ABCoordinates[1];

        Point maxTop = new Point();
        maxTop.y = properties.getTableBandTop();
        maxTop.x = ((properties.getTableBandTop() - b) / a);

        Point maxBot = new Point();
        maxBot.y = properties.getTableBandBottom();
        maxBot.x = (properties.getTableBandBottom() - b) / a;

        Point maxLeft = new Point();
        maxLeft.x = properties.getTableBandLeft();
        maxLeft.y = properties.getTableBandLeft() * a + b;

        Point maxRight = new Point();
        maxRight.x = properties.getTableBandRight();
        maxRight.y = properties.getTableBandRight() * a + b;

        if (bandsService.isPointInsideBand(maxTop)) {
            extendedLine.setPoint(maxTop);
        }

        if (bandsService.isPointInsideBand(maxLeft)) {
            extendedLine.setPoint(maxLeft);
        }

        if (bandsService.isPointInsideBand(maxBot)) {
            extendedLine.setPoint(maxBot);
        }

        if (bandsService.isPointInsideBand(maxRight)) {
            extendedLine.setPoint(maxRight);
        }

        if (extendedLine.getBegin() == null) {
            extendedLine.setBegin(new Point(0, 0));
            throw new ExtendLineException("Extended line for both sides begin point is null.");
        }

        if (extendedLine.getEnd() == null) {
            extendedLine.setEnd(new Point(0, 0));
            throw new ExtendLineException("Extended line for both sides end point is null.");
        }

        return extendedLine;
    }

    public static void safeMoveLineForVertical(Line origin) {

        if (origin.getBegin().x == origin.getEnd().x) {
            origin.getBegin().x += 3;
        }
    }

    private Point getCrosscutPointForLines(Line line1, Line line2) throws LineServiceException {

        double[] ABCoordinatesForLine1 = calcCoordinatesAB(line1);

        double[] ABCoordinatesForLine2 = calcCoordinatesAB(line2);

        if (ABCoordinatesForLine1[0] == ABCoordinatesForLine2[0]) {
            throw new LineServiceException("Error while trying find cross point for 2 lines. The lines are parallel.");
        }

        double X = (ABCoordinatesForLine2[1] - ABCoordinatesForLine1[1]) / (ABCoordinatesForLine1[0] - ABCoordinatesForLine2[0]);
        double Y = ABCoordinatesForLine1[0] * X + ABCoordinatesForLine1[1];

        return new Point(X, Y);
    }

    public static double[] calcCoordinatesAB(Line line) {
        if (line.getBegin().x == line.getEnd().x) {
            line.setEnd(new Point(line.getEnd().x + 3, line.getEnd().y));
        }

        double Y = (line.getBegin().y - line.getEnd().y);
        double X = (line.getBegin().x - line.getEnd().x);

        double A = Y / (X == 0 ? 0.1 : X);
        double B = line.getBegin().y - (A * line.getBegin().x);

        return new double[]{A, B};
    }

    public static double getDistanceBetweenPoints(Point point1, Point point2) {
        // pierw((x1 - x2)^2 + (y1 - y2)^2))
        return Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2));
    }

    /**
     * Calculate A coordinate from Ax + By + C = 0 for perpendicular line
     *
     * @param line line
     *
     * @return A coordinate
     */
    public static double calcPerpendicularCoordinate(Line line) {
        double[] coordinates = LineService.calcCoordinatesAB(line);

        return (-1 / coordinates[0]);
    }

    /**
     * Check if point is above line
     *
     * @param a     coordinate a
     * @param b     coordinate b
     * @param point point
     *
     * @return true if point is above the line
     */
    public static boolean isPointAboveTheLine(double a, double b, Point point) {
        return point.y > a * point.x + b;
    }

    public static boolean isPointAboveTheLine(Line line, Point point) {
        double[] coordinatesAB = LineService.calcCoordinatesAB(line);

        return point.y > coordinatesAB[0] * point.x + coordinatesAB[1];
    }
}
