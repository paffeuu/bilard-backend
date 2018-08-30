package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.TunnelMakerException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;
import java.util.List;

@Service
public class TargetLineService {

    static final Logger LOGGER = LoggerFactory.getLogger(TableStoryService.class);
    private LineService lineService;
    private Properties properties;

    private Point[] targetEnds;
    private int targetEndCurrentIndex;
    private List<Line> targetLines;
    private int indexOfTargetLine;

    private Line averageLine;
    private Line maxSideLine;
    private Line minSideLine;

    private int nullGivenCounter;

    @Autowired
    public TargetLineService(
            LineService lineService,
            Properties properties
    ) {
        this.properties = properties;
        this.lineService = lineService;

        this.targetEnds = new Point[properties.getTargetLineStabilizeCount()];
        this.targetEndCurrentIndex = 0;
        this.targetLines = new ArrayList<>(properties.getCountOfTargetLines());
        this.indexOfTargetLine = 0;

        this.averageLine = null;
        this.maxSideLine = null;
        this.minSideLine = null;

        this.nullGivenCounter = 0;
    }

    public Line getAverageLine() {
        return averageLine;
    }

    public Line getMaxSideLine() {
        return maxSideLine;
    }

    public Line getMinSideLine() {
        return minSideLine;
    }

    public void saveLastTargetLine(Line targetLine) {
        if (targetLine != null) {
            stabilizeTargetLine(targetLine);

            if (targetLines.isEmpty() ||
                    targetLines.size() < properties.getCountOfTargetLines()){
                targetLines.add(targetLine);
            } else {
                targetLines.set(indexOfTargetLine, targetLine);
            }
            indexOfTargetLine = (indexOfTargetLine + 1) % properties.getCountOfTargetLines();

            setCenterLine();
            makeTunnel();
        } else if (nullGivenCounter++ >= properties.getTargetNullMaxCount()) {
            nullGivenCounter = 0;
            indexOfTargetLine = 0;
            targetLines.clear();

            averageLine = null;
            minSideLine = null;
            maxSideLine = null;
        }
    }

    private Line stabilizeTargetLine(Line targetLine) {
        if (targetLine != null) {
            targetEnds[targetEndCurrentIndex++] = targetLine.getEnd();
            targetEndCurrentIndex = targetEndCurrentIndex % properties.getTargetLineStabilizeCount();

            double sumOfXsApproved = 0;
            double sumOfYsApproved = 0;
            int approvedCounter = 0;

            for (int i = 0; i < properties.getTargetLineStabilizeCount() - 1; i++){
                int tempIndex = (targetEndCurrentIndex + i) % properties.getTargetLineStabilizeCount();

                if (targetEnds[tempIndex] != null) {
                    approvedCounter++;
                    sumOfXsApproved += targetEnds[tempIndex].x;
                    sumOfYsApproved += targetEnds[tempIndex].y;
                }
            }

            if (approvedCounter >= properties.getTargetLineStabilizeCount() / 2) {
                targetLine.setEnd(new Point(sumOfXsApproved / approvedCounter, sumOfYsApproved / approvedCounter));
            }

            Line stabileLine = null;
            try {
                stabileLine = lineService.getExtendedStickLineForOneSide(targetLine);
            } catch (LineServiceException e) {
                LOGGER.warn("Cannot extend stabile line: " + stabileLine + "; Null is back", e);
            }
            return stabileLine;
        }

        return null;
    }

    private void makeTunnel() {
        if (targetLines.size() >= 2) {
            double firstDist = 0;
            double secondDist = 0;

            for (Line line : targetLines) {
                double dist = LineService.getDistanceBetweenPoints(line.getEnd(), averageLine.getEnd());
                if (LineService.isPointAboveTheLine(averageLine, line.getEnd())) {
                    if (dist >= firstDist) {
                        firstDist = dist;
                        maxSideLine = line;
                    }
                } else {
                    if (dist >= secondDist) {
                        secondDist = dist;
                        minSideLine = line;
                    }
                }
            }

            try {
                maxSideLine = lineService.getExtendedStickLineForOneSide(maxSideLine);
                minSideLine = lineService.getExtendedStickLineForOneSide(minSideLine);
            } catch (LineServiceException e) {
                LOGGER.error("Cannot extend target left or right lines.", e);
            }
        }
    }

    private void setCenterLine() {
        Point centerEndPoint = getTargetFieldCenter();

        if (centerEndPoint != null) {
            Line centerLine = new Line(targetLines.get(0).getBegin(), centerEndPoint);

            try {
                centerLine = lineService.getExtendedStickLineForOneSide(centerLine);
                centerEndPoint = centerLine.getEnd();
                averageLine = centerLine;
            } catch (LineServiceException e) {
                LOGGER.error("Cannot prepare tunnel. Center: " + centerEndPoint + " and line: " + centerLine);
            }
        }
    }

    private Point getTargetFieldCenter(){
        Point center = null;

        if (!targetLines.isEmpty()){
            double sumXs = 0;
            double sumYs = 0;
            for (Line line : targetLines) {
                sumXs += line.getEnd().x;
                sumYs += line.getEnd().y;
            }
            center = new Point(sumXs / targetLines.size(), sumYs / targetLines.size());
        }

        return center;
    }

}
