package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;
import java.util.List;

@Service
public class CueService {

    final static Logger LOGGER = LoggerFactory.getLogger(CueService.class);

    private Properties properties;

    private LineService lineService;

    @Autowired
    public CueService(
            Properties properties,
            LineService lineService
    ){
        this.properties = properties;
        this.lineService = lineService;
    }

    private double calcAbsoluteDistance(double value1, double value2){
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
        for (int i = 0; i < innerLines.size() - 1; i++){
            for (int j = 0; j < innerLines.size(); j++){
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

        if (cueLine == null){
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

        return ((min1 + min2)/2);
    }

    private double getDistanceBetweenPoints(Point point1, Point point2) {
        return Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2));
    }

    private double getMinWithNoFirst(double discardThisMinValue, double ... values){
        double temp = Double.MAX_VALUE;
        for (int i = 0; i < values.length; i++){
            if (values[i] < temp && values[i] != discardThisMinValue) {
                temp = values[i];
            }
        }
        return temp;
    }

}
