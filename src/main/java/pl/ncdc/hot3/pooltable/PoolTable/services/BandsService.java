package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Double> getDistsToBands(Point point) {
        Map<String, Double> distances = new HashMap<>();

        distances.put("LEFT", Math.abs(point.x - properties.getTableBandLeft()));
        distances.put("RIGHT", Math.abs(point.x - properties.getTableBandRight()));
        distances.put("TOP", Math.abs(point.y - properties.getTableBandTop()));
        distances.put("BOTTOM", Math.abs(point.y - properties.getTableBandBottom()));

        return distances;
    }

    public String getClosestBandName(Map<String, Double> dists) {
        Map.Entry<String, Double> min = null;
        for (Map.Entry<String, Double> entry : dists.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }

        if (min != null) {
            return min.getKey();
        }

        return null;
    }

    public PocketPosition getClosestPocketForPoint(Point point) {
        HashMap<PocketPosition, Double> distsToPockets = new HashMap<>();

        distsToPockets.put(PocketPosition.LEFT_TOP, LineService.getDistanceBetweenPoints(point, properties.getLeftTopPocketPoint()));
        distsToPockets.put(PocketPosition.RIGHT_TOP, LineService.getDistanceBetweenPoints(point, properties.getRightTopPocketPoint()));
        distsToPockets.put(PocketPosition.LEFT_BOT, LineService.getDistanceBetweenPoints(point, properties.getLeftBotPocketPoint()));
        distsToPockets.put(PocketPosition.RIGHT_BOT, LineService.getDistanceBetweenPoints(point, properties.getRightBotPocketPoint()));
        distsToPockets.put(PocketPosition.MID_TOP, LineService.getDistanceBetweenPoints(point, properties.getMidTopPocketPoint()));
        distsToPockets.put(PocketPosition.MID_BOT, LineService.getDistanceBetweenPoints(point, properties.getMidBotPocketPoint()));

        Map.Entry<PocketPosition, Double> min = null;
        for (Map.Entry<PocketPosition, Double> entry : distsToPockets.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }

        return min.getKey();
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

    public Point getPointForPocketEnum(PocketPosition pocketPosition) {
        Point pocketPoint;
        switch (pocketPosition) {
            case LEFT_TOP:
                pocketPoint = properties.getLeftTopPocketPoint();
                break;
            case MID_TOP:
                pocketPoint = properties.getMidTopPocketPoint();
                break;
            case RIGHT_TOP:
                pocketPoint = properties.getRightTopPocketPoint();
                break;
            case LEFT_BOT:
                pocketPoint = properties.getLeftBotPocketPoint();
                break;
            case MID_BOT:
                pocketPoint = properties.getMidBotPocketPoint();
                break;
            case RIGHT_BOT:
                pocketPoint = properties.getRightBotPocketPoint();
                break;
            case NONE:
                pocketPoint = null;
                break;
            default:
                pocketPoint = null;
                break;
        }
        return pocketPoint;
    }

}
