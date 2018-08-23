package pl.ncdc.hot3.pooltable.PoolTable.services.Settings;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        double distToSocketTolerance = properties.getTablePocketRadius();

        if (LineService.getDistanceBetweenPoints(point, properties.getLeftTopPocketPoint()) <= distToSocketTolerance)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, properties.getRightTopPocketPoint()) <= distToSocketTolerance)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, properties.getLeftBotPocketPoint()) <= distToSocketTolerance)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, properties.getRightBotPocketPoint()) <= distToSocketTolerance)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, properties.getMidTopPocketPoint()) <= distToSocketTolerance)
            isInSocket = true;
        else if (LineService.getDistanceBetweenPoints(point, properties.getMidBotPocketPoint()) <= distToSocketTolerance)
            isInSocket = true;

        return isInSocket;
    }

}
