package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.ExtendLineException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@Service
public class LineService {
    @Autowired
    private Properties properties;

    @Autowired
    private Detector detector;

    public Line getDirectedLine(Line a, Line b) throws CueServiceException {
        Line extendedA = getExtendedStickLineForOneSide(a);
        Line extendedB = getExtendedStickLineForOneSide(b);

        double distance1 = calculateDistanceBetweenPoints(extendedA.getBegin(), extendedB.getBegin());
        double distance2 = calculateDistanceBetweenPoints(extendedA.getBegin(), extendedB.getEnd());
        double distance3 = calculateDistanceBetweenPoints(extendedA.getEnd(), extendedB.getBegin());
        double distance4 = calculateDistanceBetweenPoints(extendedA.getEnd(), extendedB.getEnd());
        double[] distances = {distance1, distance2, distance3, distance4};
        double minDistance = distance1;

        for (double distance : distances) {
            if (minDistance > distance) {
                minDistance = distance;
            }
        }

        if (minDistance == distance1) {
            extendedA = switchPoints(extendedA);
            extendedB = switchPoints(extendedB);
        } else if(minDistance == distance2) {
            extendedA = switchPoints(extendedA);
        } else if (minDistance == distance3) {
            extendedB = switchPoints(extendedB);
        }

        Point newLineStart = new Point(
                (extendedA.getBegin().x + extendedB.getBegin().x) / 2,
                (extendedA.getBegin().y + extendedB.getBegin().y) / 2
        );

        Point newLineEnd = new Point(
                (extendedA.getEnd().x + extendedB.getEnd().x) / 2,
                (extendedA.getEnd().y + extendedB.getEnd().y) / 2
        );

        return getExtendedStickLineForBothSides(new Line(newLineStart, newLineEnd));
    }

    public static double calculateDistanceBetweenPoints(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public static Line switchPoints(Line a) {
        Point tmp = a.getBegin();
        a.setBegin(a.getEnd());
        a.setEnd(tmp);

        return a;
    }

    public Line getExtendedStickLineForOneSide(Line stickLine) throws LineServiceException {
        double horizontalMove = stickLine.getEnd().x - stickLine.getBegin().x;
        double verticalMove = stickLine.getEnd().y - stickLine.getBegin().y;

        safeMoveLine(stickLine, horizontalMove, verticalMove);

        Line extendedLine = new Line();
        Point crosscutPoint1 = null;
        Point crosscutPoint2 = null;

        Point leftTopCornerPoint = new Point(properties.getTableBandLeft(), properties.getTableBandTop());
        Point rightTopCornerPoint = new Point(properties.getTableBandRight(), properties.getTableBandTop());
        Point rightBotCornerPoint = new Point(properties.getTableBandRight(), properties.getTableBandBottom());
        Point leftBotCornerPoint = new Point(properties.getTableBandLeft(), properties.getTableBandBottom());

        Line topBand = new Line(leftTopCornerPoint, rightTopCornerPoint);
        Line rightBand = new Line(rightTopCornerPoint, rightBotCornerPoint);
        Line botBand = new Line(leftBotCornerPoint, rightBotCornerPoint);
        Line leftBand = new Line(leftBotCornerPoint, leftTopCornerPoint);

        if (horizontalMove >= 0 && verticalMove <= 0){

            crosscutPoint1 = getCrosscutPointForLines(stickLine, topBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, rightBand);

        } else if (horizontalMove < 0 && verticalMove > 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, leftBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, botBand);

        } else if (horizontalMove < 0 && verticalMove < 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, leftBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, topBand);

        } else if (horizontalMove >= 0 && verticalMove >= 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, rightBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, botBand);

        }

        try {
            if (detector.isPointInsideBand(crosscutPoint1)) {
                extendedLine.setBegin(stickLine.getEnd());
                extendedLine.setEnd(crosscutPoint1);
            } else if (detector.isPointInsideBand(crosscutPoint2)) {
                extendedLine.setBegin(stickLine.getEnd());
                extendedLine.setEnd(crosscutPoint2);
            } else {
                throw new ExtendLineException("Error while trying make extended line for one side. Both crosscut points out of the bands");
            }
        }
        catch (NullPointerException e) {
            throw new ExtendLineException("Make extended line for one side error. Points still null value.");
        }

        return extendedLine;
    }

    public Line getExtendedStickLineForBothSides(Line stickLine) throws LineServiceException {

        Line extendedOneSide = getExtendedStickLineForOneSide(stickLine);

        switchPoints(extendedOneSide);

        Line extendedBothSides = getExtendedStickLineForOneSide(extendedOneSide);

        switchPoints(extendedBothSides);

        return extendedBothSides;
    }

    private void safeMoveLine(Line origin, double horizontalMove, double verticalMove){
        Point newBegin = origin.getBegin();

        if (horizontalMove == 0) {
            newBegin.x += 3;
        }

        if (verticalMove == 0) {
            newBegin.y += 3;
        }

        origin.setBegin(newBegin);
    }

    private Point getCrosscutPointForLines(Line line1, Line line2) throws LineServiceException {

        double a_Line1 = calcCoordinate_A(line1);
        double b_Line1 = line1.getBegin().y - (a_Line1*line1.getBegin().x);

        double a_Line2 = calcCoordinate_A(line2);
        double b_Line2 = line2.getBegin().y - (a_Line2*line2.getBegin().x);

        if (a_Line1 == a_Line2) {
            throw new LineServiceException("Error while trying find cross point for 2 lines. The lines are parallel.");
        }

        double X = (b_Line2 - b_Line1) / (a_Line1 - a_Line2);
        double Y = a_Line1 * X + b_Line1;

        return new Point(X, Y);
    }

    public double calcCoordinate_A(Line line){

        if (line.getBegin().x == line.getEnd().x){
            line.setEnd(new Point(line.getEnd().x + 3, line.getEnd().y));
        }

        double Y = (line.getBegin().y - line.getEnd().y);
        double X = (line.getBegin().x - line.getEnd().x);

        return (Y/X);
    }
}
