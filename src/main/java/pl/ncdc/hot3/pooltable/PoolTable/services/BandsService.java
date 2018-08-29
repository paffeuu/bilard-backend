package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

import java.util.ArrayList;
import java.util.List;

@Service
public class BandsService {

    private Properties properties;

    public enum PocketPosition {
        NONE,
        LEFT_TOP,
        MID_TOP,
        RIGHT_TOP,
        RIGHT_BOT,
        MID_BOT,
        LEFT_BOT
    }

    @Autowired
    public BandsService(
            Properties properties
    ) {
        this.properties = properties;
    }

    public boolean isPointInsideBand(Point point){
       return isPointInsideBand(point, 5);
    }

    public boolean isPointInsideBand(Point point, int tolerance) {
        if (point.x >= properties.getTableBandLeft()  - tolerance && point.x <= properties.getTableBandRight()  + tolerance) {
            if (point.y >= properties.getTableBandTop() - tolerance && point.y <= properties.getTableBandBottom() + tolerance) {
                return true;
            }
        }
        return false;
    }

    public PocketPosition getPocketForPoint(Point point) {
        PocketPosition inPocketPosition = PocketPosition.NONE;
        double distToSocketTolerance = properties.getTablePocketRadius();

        if (LineService.getDistanceBetweenPoints(point, properties.getLeftTopPocketPoint()) <= distToSocketTolerance)
            inPocketPosition = PocketPosition.LEFT_TOP;
        else if (LineService.getDistanceBetweenPoints(point, properties.getRightTopPocketPoint()) <= distToSocketTolerance)
            inPocketPosition = PocketPosition.RIGHT_TOP;
        else if (LineService.getDistanceBetweenPoints(point, properties.getLeftBotPocketPoint()) <= distToSocketTolerance)
            inPocketPosition = PocketPosition.LEFT_BOT;
        else if (LineService.getDistanceBetweenPoints(point, properties.getRightBotPocketPoint()) <= distToSocketTolerance)
            inPocketPosition = PocketPosition.RIGHT_BOT;
        else if (LineService.getDistanceBetweenPoints(point, properties.getMidTopPocketPoint()) <= distToSocketTolerance)
            inPocketPosition = PocketPosition.MID_TOP;
        else if (LineService.getDistanceBetweenPoints(point, properties.getMidBotPocketPoint()) <= distToSocketTolerance)
            inPocketPosition = PocketPosition.MID_BOT;

        return inPocketPosition;
    }

    public List <Line> getBandLines(){
        List <Line> bandLines = new ArrayList<>();

        Point leftTop = new Point(properties.getTableBandLeft(), properties.getTableBandTop());
        Point rightTop = new Point(properties.getTableBandRight(), properties.getTableBandTop());
        Point leftBot = new Point(properties.getTableBandLeft(), properties.getTableBandBottom());
        Point rightBot = new Point(properties.getTableBandRight(), properties.getTableBandBottom());

        bandLines.add(new Line(leftTop, rightTop));
        bandLines.add(new Line(rightTop, rightBot));
        bandLines.add(new Line(rightBot, leftBot));
        bandLines.add(new Line(leftBot, leftTop));

        return bandLines;
    }

}
