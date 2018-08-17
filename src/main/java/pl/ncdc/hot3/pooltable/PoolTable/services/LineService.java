package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.ExtendLineException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;

@Service
public class LineService {
    private Properties properties;

    private Line prevExtendedCueLines[] = new Line[10];
    private int prevLinesCounter = 0, prevLinesMax = 10;
    private double previousEndPointDistTolerance = 5;

    @Autowired
    public LineService(Properties properties) {
        this.properties = properties;
    }

    public LineService() {}

    public Line getDirectedLine(Line a, Line b) throws LineServiceException {
        Line extendedA = getExtendedStickLineForBothSides(a);
        Line extendedB = getExtendedStickLineForBothSides(b);

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

        Line finalLine = makeSureLineEndCorrect(new Line(newLineStart, newLineEnd));

        //finalLine = getExtendedStickLineForOneSide(finalLine);

        return finalLine;
    }

    private Line makeSureLineEndCorrect(Line finalLine){

        int countOfCorrectLines = 0;
        int countOfSwitchedLines = 0;
        for (int i = 0; i < prevLinesMax; i++){
            if (prevExtendedCueLines[(prevLinesCounter + i) % 10] != null){
                Point beginPoint = prevExtendedCueLines[(prevLinesCounter + i) % 10].getBegin();
                Point endPoint = prevExtendedCueLines[(prevLinesCounter + i) % 10].getEnd();

                if (calculateDistanceBetweenPoints(finalLine.getEnd(), endPoint) <= previousEndPointDistTolerance){
                    countOfCorrectLines++;
                } else if (calculateDistanceBetweenPoints(finalLine.getEnd(), beginPoint) <= previousEndPointDistTolerance){
                    countOfSwitchedLines++;
                }
            }
        }
        if (countOfSwitchedLines > countOfCorrectLines){
            switchPoints(finalLine);
        }
        prevLinesCounter = (prevLinesCounter+1) % prevLinesMax;
        prevExtendedCueLines[prevLinesCounter] =  finalLine;
        return finalLine;
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

        double Y = (stickLine.getBegin().y - stickLine.getEnd().y);
        double X = (stickLine.getBegin().x - stickLine.getEnd().x);

        double a = Y / X;
        double b = stickLine.getBegin().y - (a * stickLine.getBegin().x);

        if (horizontalMove >= 0 && verticalMove <= 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, topBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, rightBand);

            Point maxTop = new Point();
            maxTop.y = properties.getTableBandTop();
            maxTop.x = ((properties.getTableBandTop() - b) / a);

            Point maxRight = new Point();
            maxRight.x = properties.getTableBandRight();
            maxRight.y = properties.getTableBandRight() * a + b;

            crosscutPoint1 = maxTop;
            crosscutPoint2 = maxRight;

//            System.out.println("============================");
//            System.out.println("lineService::getExtendedStickLineForOneSide()");
//            System.out.println(stickLine.toString());
//            System.out.println(crosscutPoint1);
//            System.out.println(crosscutPoint2);
//            System.out.println("----------------------------");
//            System.out.println(maxTop);
//            System.out.println(maxRight);
//            System.out.println(properties.isPointInsideBand(crosscutPoint1) + ", " + properties.isPointInsideBand(maxTop));
//            System.out.println(properties.isPointInsideBand(crosscutPoint2) + ", " + properties.isPointInsideBand(maxRight));
//            System.out.println(properties.getTableBandLeft() + ", " + properties.getTableBandTop());
//            System.out.println(", " + properties.getTableBandRight() + ", " + properties.getTableBandBottom());
//            System.out.println("1===========================");

        } else if (horizontalMove < 0 && verticalMove > 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, leftBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, botBand);

            Point maxBot = new Point();
            maxBot.y = properties.getTableBandBottom();
            maxBot.x = (properties.getTableBandBottom() - b) / a;

            Point maxLeft = new Point();
            maxLeft.x = properties.getTableBandLeft();
            maxLeft.y = properties.getTableBandLeft() * a + b;

            crosscutPoint1 = maxLeft;
            crosscutPoint2 = maxBot;
            //           System.out.println("2===========================");
//            System.out.println("lineService::getExtendedStickLineForOneSide()");
//            System.out.println(stickLine.toString());
//            System.out.println(crosscutPoint1);
//            System.out.println(crosscutPoint2);
//            System.out.println("----------------------------");
//            System.out.println(maxLeft);
//            System.out.println(maxBot);
//            System.out.println(properties.isPointInsideBand(crosscutPoint1) + ", " + properties.isPointInsideBand(maxLeft));
//            System.out.println(properties.isPointInsideBand(crosscutPoint2) + ", " + properties.isPointInsideBand(maxBot));
//            System.out.println("============================");

        } else if (horizontalMove < 0 && verticalMove < 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, leftBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, topBand);

            Point maxTop = new Point();
            maxTop.y = properties.getTableBandTop();
            maxTop.x = ((properties.getTableBandTop() - b) / a);

            Point maxLeft = new Point();
            maxLeft.x = properties.getTableBandLeft();
            maxLeft.y = properties.getTableBandLeft() * a + b;
            //          System.out.println("33===========================");

            crosscutPoint1 = maxLeft;
            crosscutPoint2 = maxTop;

        } else if (horizontalMove >= 0 && verticalMove >= 0) {

            crosscutPoint1 = getCrosscutPointForLines(stickLine, rightBand);
            crosscutPoint2 = getCrosscutPointForLines(stickLine, botBand);

            Point maxBot = new Point();
            maxBot.y = properties.getTableBandBottom();
            maxBot.x = (properties.getTableBandBottom() - b) / a;

            Point maxRight = new Point();
            maxRight.x = properties.getTableBandRight();
            maxRight.y = properties.getTableBandRight() * a + b;
            //          System.out.println("444===========================");

            crosscutPoint1 = maxRight;
            crosscutPoint2 = maxBot;

        }

        try {
            if (properties.isPointInsideBand(crosscutPoint1)) {
                extendedLine.setBegin(stickLine.getBegin());
                extendedLine.setEnd(crosscutPoint1);
            } else if (properties.isPointInsideBand(crosscutPoint2)) {
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

        double Y = (stickLine.getBegin().y - stickLine.getEnd().y);
        double X = (stickLine.getBegin().x - stickLine.getEnd().x);

        double a = Y / X;
        double b = stickLine.getBegin().y - (a * stickLine.getBegin().x);

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

        if (properties.isPointInsideBand(maxTop)) {
            extendedLine.setPoint(maxTop);
        }

        if (properties.isPointInsideBand(maxLeft)) {
            extendedLine.setPoint(maxLeft);
        }

        if (properties.isPointInsideBand(maxBot)) {
            extendedLine.setPoint(maxBot);
        }

        if (properties.isPointInsideBand(maxRight)) {
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


    private void safeMoveLine(Line origin, double horizontalMove, double verticalMove) {
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
        double b_Line1 = line1.getBegin().y - (a_Line1 * line1.getBegin().x);

        double a_Line2 = calcCoordinate_A(line2);
        double b_Line2 = line2.getBegin().y - (a_Line2 * line2.getBegin().x);

        if (a_Line1 == a_Line2) {
            throw new LineServiceException("Error while trying find cross point for 2 lines. The lines are parallel.");
        }

        double X = (b_Line2 - b_Line1) / (a_Line1 - a_Line2);
        double Y = a_Line1 * X + b_Line1;

        return new Point(X, Y);
    }

    public static double calcCoordinate_A(Line line) {

        if (line.getBegin().x == line.getEnd().x) {
            line.setEnd(new Point(line.getEnd().x + 3, line.getEnd().y));
        }

        double Y = (line.getBegin().y - line.getEnd().y);
        double X = (line.getBegin().x - line.getEnd().x);

        return (Y / X);
    }
}
