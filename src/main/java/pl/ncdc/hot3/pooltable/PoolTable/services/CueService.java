package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;
import java.util.List;

@Service
public class CueService {

    final static Logger LOGGER = LoggerFactory.getLogger(CueService.class);

    private Properties properties;

    private Detector detector;

    private LineService lineService;

    @Autowired
    public CueService(
            Properties properties,
            Detector detector,
            LineService lineService
    ){
        this.properties = properties;
        this.detector = detector;
    }

    public Line predictTrajectoryAfterBump(Line line)  {
        Point bumpPoint = line.getEnd();
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

    public List<Line> getStickWithPredictions(int deep) throws DetectorException, CueServiceException, LineServiceException {
        Line cue = detector.findStickLine();

        List <Line> predictions = new ArrayList<>();

        if (cue != null) {
            predictions.add(cue);

            for (int i = 0; i < deep; i++){
                Line pred = predictTrajectoryAfterBump(predictions.get(i));
                predictions.add(pred);
            }
        }

        return predictions;
    }

}
