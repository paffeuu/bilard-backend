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
        this.lineService = lineService;
    }

    private double calcAbsoluteDistance(double value1, double value2){
        return Math.abs(value1 - value2);
    }

    public Line predictTrajectoryAfterBump(Line line) throws LineServiceException {

        Point bumpPoint = line.getEnd();
        Point halfDistance = new Point(0, 0);

        if (calcAbsoluteDistance(properties.getTableBandLeft(), bumpPoint.x) <= 2) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (calcAbsoluteDistance(properties.getTableBandRight(), bumpPoint.x) <= 2) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (calcAbsoluteDistance(properties.getTableBandTop(), bumpPoint.y) <= 2) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (calcAbsoluteDistance(properties.getTableBandBottom(), bumpPoint.y) <= 2) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else {
            LOGGER.warn("Cannot find predicted line. Bump point out of bands!");
        }

        double distanceX = (halfDistance.x - line.getBegin().x);
        double distanceY = (halfDistance.y - line.getBegin().y);

        return new Line(
                bumpPoint,
                new Point(halfDistance.x + distanceX, halfDistance.y + distanceY)
        );
    }

    public List<Line> getStickWithPredictions() {
        List <Line> predictions = new ArrayList<>();

        try {
            Line cue = detector.findStickLine();

            predictions.add(cue);

            for (int i = 0; i < properties.getPredictionDepth(); i++){
                Line pred = predictTrajectoryAfterBump(predictions.get(i));
                predictions.add(pred);
            }

        } catch (MissingCueLineException e) {
            LOGGER.warn("Cannot found stick line.");
        } finally {
            return predictions;
        }
    }

}
