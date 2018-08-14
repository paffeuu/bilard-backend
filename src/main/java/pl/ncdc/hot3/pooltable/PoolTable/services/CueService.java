package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@Service
public class CueService {

    final static Logger LOGGER = LoggerFactory.getLogger(CueService.class);

    //@Autowired
    private Properties properties;

   // @Autowired
    private Detector detector;

    @Autowired
    public CueService(
            Properties properties,
            Detector detector
    ){
        this.properties = properties;
        this.detector = detector;
    }

    private Point getCrosscutPointForLines(Line line1, Line line2) throws CueServiceException {

        double a_Line1 = detector.calcCoordinate_A(line1);
        double b_Line1 = line1.getBegin().y - (a_Line1*line1.getBegin().x);

        double a_Line2 = detector.calcCoordinate_A(line2);
        double b_Line2 = line2.getBegin().y - (a_Line2*line2.getBegin().x);

        if (a_Line1 == a_Line2) {
            throw new CueServiceException("The lines are parallel.");
        }

        double X = (b_Line2 - b_Line1) / (a_Line1 - a_Line2);
        double Y = a_Line1 * X + b_Line1;

        return new Point(X, Y);
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

    public Line getExtendedStickLineForOneSide(Line stickLine) throws CueServiceException {
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

        System.out.println(crosscutPoint1);
        System.out.println(crosscutPoint2);

        try {
            if (detector.isPointInsideBand(crosscutPoint1)) {
                extendedLine.setBegin(stickLine.getEnd());
                extendedLine.setEnd(crosscutPoint1);
            } else if (detector.isPointInsideBand(crosscutPoint2)) {
                extendedLine.setBegin(stickLine.getEnd());
                extendedLine.setEnd(crosscutPoint2);
            } else {
                throw new CueServiceException("Error while trying make extended line for one side. Both crosscut points out of the bands");
            }
        }
        catch (NullPointerException e) {
            throw new CueServiceException("Make extended line for one side error. Points still null value.");
        }

        return extendedLine;
    }

    public Line getExtendedStickLineForBothSides(Line stickLine) throws CueServiceException {

        Line extendedLine = new Line();

        double Y = (stickLine.getBegin().y - stickLine.getEnd().y);
        double X = (stickLine.getBegin().x - stickLine.getEnd().x);

        safeMoveLine(stickLine, X, Y);

        double a = Y/X;
        double b = stickLine.getBegin().y - (a*stickLine.getBegin().x);

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

        if (detector.isPointInsideBand(maxTop)){
            extendedLine.setPoint(maxTop);
        }

        if (detector.isPointInsideBand(maxLeft)){
            extendedLine.setPoint(maxLeft);
        }

        if (detector.isPointInsideBand(maxBot)){
            extendedLine.setPoint(maxBot);
        }

        if (detector.isPointInsideBand(maxRight)){
            extendedLine.setPoint(maxRight);
        }

        if (extendedLine.getBegin() == null) {
            extendedLine.setBegin(new Point(0, 0));
            throw new CueServiceException("Extended line for both sides begin point is null.");
        }

        if (extendedLine.getEnd() == null) {
            extendedLine.setEnd(new Point(0, 0));
            throw new CueServiceException("Extended line for both sides end point is null.");
        }

        return extendedLine;
    }



    public Line predictTrajectoryAfterBump(Point bumpPoint, Line line) throws CueServiceException {
        Point halfDistance = new Point(0, 0);

        if (properties.getTableBandLeft() == bumpPoint.x) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (properties.getTableBandRight() == bumpPoint.x) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (properties.getTableBandTop() == bumpPoint.y) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (properties.getTableBandBottom() == bumpPoint.y) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else {
           // throw new CueServiceException("Band not found");
        }

        double distanceX = (halfDistance.x - line.getBegin().x);
        double distanceY = (halfDistance.y - line.getBegin().y);

        return new Line(
                bumpPoint,
                new Point(halfDistance.x + distanceX, halfDistance.y + distanceY)
        );
    }

}
