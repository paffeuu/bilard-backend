package pl.ncdc.hot3.pooltable.PoolTable.services.Settings;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

@Service
public class BandsService {

    private Properties properties;

    @Autowired
    public BandsService(
            Properties properties
    ) {
        this.properties = properties;
    }

    public boolean isPointInsideBand(Point point){
        if (point.x >= properties.getTableBandLeft() - 5 && point.x <= properties.getTableBandRight() + 5) {
            if (point.y >= properties.getTableBandTop() - 5 && point.y <= properties.getTableBandBottom() + 5) {
                return true;
            }
        }
        return false;
    }

    public boolean isPointGoingToSocket(Point point) {
        boolean isInSocket = false;
        double distToSocketTollerace = 45;

        Point leftTop = new Point(properties.getTableBandLeft(), properties.getTableBandTop());
        Point rightTop = new Point(properties.getTableBandRight(), properties.getTableBandTop());
        Point leftBot = new Point(properties.getTableBandLeft(), properties.getTableBandBottom());
        Point rightBot = new Point(properties.getTableBandRight(), properties.getTableBandBottom());
        Point midTop = new Point((properties.getTableBandLeft() + properties.getTableBandRight()) / 2, properties.getTableBandTop());
        Point midBot = new Point((properties.getTableBandLeft() + properties.getTableBandRight()) / 2, properties.getTableBandBottom());

        if (LineService.getDistanceBetweenPoints(point, leftTop) <= distToSocketTollerace)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, rightTop) <= distToSocketTollerace)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, leftBot) <= distToSocketTollerace)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, rightBot) <= distToSocketTollerace)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, midTop) <= distToSocketTollerace)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, midBot) <= distToSocketTollerace)
            isInSocket = true;

        return isInSocket;
    }

}
