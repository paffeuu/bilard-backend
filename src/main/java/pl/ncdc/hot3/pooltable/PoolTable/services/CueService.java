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

    private LineService lineService;

    @Autowired
    public CueService(
            Properties properties,
            LineService lineService
    ) {
        this.properties = properties;
        this.lineService = lineService;
    }

    private double calcAbsoluteDistance(double value1, double value2) {
        return Math.abs(value1 - value2);
    }

    public Line predictTrajectoryAfterBump(Line line) throws CueServiceException, LineServiceException {

        Point bumpPoint = line.getEnd();
        Point halfDistance;

        if (calcAbsoluteDistance(properties.getTableBandLeft(), bumpPoint.x) <= 2) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (calcAbsoluteDistance(properties.getTableBandRight(), bumpPoint.x) <= 2) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (calcAbsoluteDistance(properties.getTableBandTop(), bumpPoint.y) <= 2) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (calcAbsoluteDistance(properties.getTableBandBottom(), bumpPoint.y) <= 2) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else {
            throw new CueServiceException("Cannot find predicted line. Bump point out of bands!");
        }

        double distanceX = (halfDistance.x - line.getBegin().x);
        double distanceY = (halfDistance.y - line.getBegin().y);

        Line predictedLine = new Line(
                bumpPoint,
                new Point(halfDistance.x + distanceX, halfDistance.y + distanceY)

        );

        predictedLine = lineService.getExtendedStickLineForOneSide(predictedLine);
        return predictedLine;

    }

    public Line findStickLine(List<Line> innerLines) throws MissingCueLineException, LineServiceException {

        Line cueLine = null;

        double dist;
        double a1, a2;

        outerloop:
        for (int i = 0; i < innerLines.size() - 1; i++) {
            for (int j = 0; j < innerLines.size(); j++) {
                if (i != j) {

                    a1 = lineService.calcCoordinate_A(innerLines.get(i));
                    a2 = lineService.calcCoordinate_A(innerLines.get(j));

                    if (Math.abs(a1 - a2) < properties.getParallelTolerance()) {
                        dist = getDistanceBetweenLines(innerLines.get(i), innerLines.get(j));
                        if (dist < properties.getCueThickness()) {
                            cueLine = lineService.getDirectedLine(innerLines.get(i), innerLines.get(j));

                            break outerloop;
                        }
                    }

                }
            }
        }

        if (cueLine == null) {
            throw new MissingCueLineException("Could not find stick line.");
        }

        return cueLine;
    }

    private double getDistanceBetweenLines(Line line1, Line line2) {

        double begin2begin = getDistanceBetweenPoints(line1.getBegin(), line2.getBegin());
        double begin2end = getDistanceBetweenPoints(line1.getBegin(), line2.getEnd());
        double end2begin = getDistanceBetweenPoints(line1.getEnd(), line2.getBegin());
        double end2end = getDistanceBetweenPoints(line1.getEnd(), line2.getEnd());

        double min1 = getMinWithNoFirst(0, begin2begin, begin2end, end2begin, end2end);
        double min2 = getMinWithNoFirst(min1, begin2begin, begin2end, end2begin, end2end);

        return ((min1 + min2) / 2);
    }

    private double getDistanceBetweenPoints(Point point1, Point point2) {
        return Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2));
    }

    private double getMinWithNoFirst(double discardThisMinValue, double... values) {
        double temp = Double.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] < temp && values[i] != discardThisMinValue) {
                temp = values[i];
            }
        }
        return temp;
    }

    public double[] calcAllCoordinate(Line line) {
        double Y = line.getBegin().y - line.getEnd().y;
        double X = line.getBegin().x - line.getEnd().x;
        double a = Y / X;
        double b = line.getBegin().y - line.getBegin().x * a;

        return new double[]{a, -1, b};
    }

    public double calculateDistanceBetweenPointAndLine(Point point, Line line) {
        double[] coordinates = calcAllCoordinate(line);

        // http://matematyka.pisz.pl/strona/1249.html
        return Math.abs(coordinates[0] * point.x + coordinates[1] * point.y + coordinates[2]) /
                Math.sqrt(Math.pow(coordinates[0], 2) + Math.pow(coordinates[1], 2));
    }

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

        return lineService.getExtendedStickLineForOneSide(new Line(
                point,
                new Point(
                        ball.getX(),
                        ball.getY()
                )
        ));
    }

    public Line refactorCueLine(Line line, Ball ball) throws LineServiceException {
        double distance = calculateDistanceBetweenPointAndLine(new Point(ball.getX(), ball.getY()), line);
        double[] coordinates = calcAllCoordinate(line);
        double[] newCoordinates = {coordinates[0], coordinates[1], coordinates[2] + distance};
        double A = newCoordinates[0];
        double B = newCoordinates[1];
        double C = newCoordinates[2];

        return lineService.getExtendedStickLineForOneSide(
                new Line(
                        new Point(
                                ball.getX(),
                                ball.getY()
                        ),
                        new Point(
                                line.getEnd().x,
                                (-C - A * (line.getEnd().x)) / B
                        )
                )
        );
    }
}
