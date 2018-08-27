package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.MockupService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@Service
public class TableStoryService {

    final static int LIMIT_OF_TABLES = 32;
    final static int LIMIT_OF_VIEWS = 6;
    final static Logger LOGGER = LoggerFactory.getLogger(TableStoryService.class);

    private int currentTableIndex;
    private List<PoolTable> tableStory;
    private Mat outputImage;
    private int counter = 0;

    private Detector detector;
    private CameraService cameraService;
    private Drawer drawer;
    private Properties properties;
    private BandsService bandsService;
    List<Ball> prevFrameBalls;

    private PreviousPositionService previousPositionService;

    @Autowired
    public TableStoryService(
            Detector detector,
            CameraService cameraService,
            Drawer drawer,
            Properties properties,
            PreviousPositionService previousPositionService,
            BandsService bandsService
    ) {
        this.detector = detector;
        this.cameraService = cameraService;
        this.drawer = drawer;
        this.properties = properties;
        this.previousPositionService = previousPositionService;
        this.bandsService = bandsService;

        this.prevFrameBalls = new ArrayList<>();
        currentTableIndex = -1;

        tableStory = new ArrayList<>(LIMIT_OF_TABLES);
    }

    private PoolTable current(int backwardStep){
        if (currentTableIndex >= backwardStep)
            return tableStory.get(currentTableIndex - backwardStep);

        return null;
    }

    private PoolTable current(){
        if (currentTableIndex >= 0)
            return tableStory.get(currentTableIndex);

        return null;
    }

    public TableStoryService next() {
        try {
            outputImage = cameraService.getSnap();
            detector.setSourceImg(outputImage.clone());
        } catch (CameraServiceException e) {
            //LOGGER.warn("Camera view not available. Empty table image as a source");
            outputImage = detector.getSourceImg().clone();
        }



        currentTableIndex = (currentTableIndex + 1) % LIMIT_OF_TABLES;

        if (tableStory.size() < LIMIT_OF_TABLES)
            tableStory.add(new PoolTable());
        else
            tableStory.set(currentTableIndex, new PoolTable());

        return this;
    }

    public TableStoryService findCue(){
        try {
            Line cue = detector.findStickLine();

            if (cue instanceof Line && null != current().getBalls()) {
                Ball collisionBall = detector.getCollisionBall(cue, current().getBalls(), false);

                if (collisionBall instanceof Ball && 0 == collisionBall.getId()) {
                    cue = detector.refactorCueLine(cue, collisionBall);
                }
            }

            current().setCue(cue);
        } catch (MissingCueLineException e) {
            LOGGER.info(e.getMessage());
        } catch (LineServiceException e) {
            LOGGER.warn("Missing line when try find cue.", e);
        } catch (DetectorException e) {
            LOGGER.error("Error while trying find cue.", e);
        }
        return this;
    }

    public TableStoryService makePredictions(){

        try {
            if (current().getCue() != null)
                current().setPredictions(detector.getPredictions(current().getCue()));
        } catch (CueServiceException e) {
            //LOGGER.warn("Predictions canceled.", e);
        } catch (LineServiceException e) {
            LOGGER.error("Cannot extend predicted line.", e);
        }
        return this;

    }

    /**
     * Detect collision and set target line based on collision
     *
     * @return this object
     */
    public TableStoryService detectCollision() {
        try {
            if (current() != null &&
                    null != current().getCue() &&
                    null != current().getBalls() &&
                    null != current().getPredictions()) {

                List<Line> currentPredictions = current().getPredictions();
                List<Line> newPredictions = new ArrayList<>();

                for (Line prediction : currentPredictions) {
                    boolean isCue = 0 == currentPredictions.indexOf(prediction); // Pierwsza predykcja to kij

                    Line targetLine = detector.createTargetLine(
                            prediction,
                            current().getBalls(),
                            isCue
                    );

                    if (null != targetLine) {
                        current().setTargetLine(targetLine);
                        Line newLine = new Line(
                                prediction.getBegin(),
                                targetLine.getBegin()
                        );

                        if (isCue) {
                            current().setCue(newLine);
                        }

                        newPredictions.add(newLine);
                        break;
                    } else {
                        newPredictions.add(prediction);
                    }
                }

                current().setPredictions(newPredictions);
            }
        } catch (LineServiceException e) {
            LOGGER.info("Can not find target line");
        }

        return this;
    }

    public TableStoryService findBalls() {
        try {
            List<Ball> balls = detector.createListOfBalls();
            current().setBalls(balls);
            prevFrameBalls = balls;
        } catch (Exception e) {
            LOGGER.error("Unknow exception for no balls on table, returned previous.");
            current().setBalls(prevFrameBalls);
        }

        return this;
    }

    public PoolTable build() {
        return  drawForDebug()
                .makeView()
                .current();
    }

    public TableStoryService showPrevious(){

        if (properties.isShowPreviousPosition()) {
            saveToPrevService();

            try {
                drawer.drawBalls(outputImage, previousPositionService.getPreviousPosition(), new Scalar(255, 0, 255));
            } catch (DrawerException e) {
                //LOGGER.warn("Could not draw previous balls possition. Nested: " + e.getMessage());
            }
        }

        return this;
    }

    private void saveToPrevService(){
        try {
            if (OpenCVBufforFlushService.getCounter() % 4 == 0) {
                previousPositionService.addPosition(current().getBalls());
                previousPositionService.updatePreviousBallPosition();
            }
        } catch (NullPointerException e) {
            //LOGGER.warn("Can not save previous position. Nested: ", e.getMessage());
        }
    }

    public TableStoryService drawForDebug(){
        if (properties.isDebugActive()) {
            drawer.drawLines(outputImage, bandsService.getBandLines(), new Scalar(255, 0, 0), 4);

            if (detector.getPointCloserToWhiteBall() != null) {
                drawer.drawPoint(outputImage, detector.getPointCloserToWhiteBall(), new Scalar(255, 0, 0), 4);
            }

            if (detector.getPointFurtherToWhiteBall() != null) {
                drawer.drawPoint(outputImage, detector.getPointFurtherToWhiteBall(), new Scalar(0, 0, 0), 2);
            }

            if (detector.getDebugAverageLine() != null){
                drawer.drawLine(outputImage, detector.getDebugAverageLine(), new Scalar(0, 255, 122), 6);
            }

            if (!detector.getDebugDetectedLines().isEmpty()) {
                drawer.drawLines(
                        outputImage,
                        detector.getDebugDetectedLines(),
                        new Scalar(0, 0, 255),
                        5
                );
            }
        }
        return this;
    }

    private TableStoryService makeView(){
        try {
            drawer.draw(
                    outputImage,
                    current().getCue(),
                    current().getBalls(),
                    current().getPredictions(),
                    current().getTargetLine()
            );
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", outputImage, matOfByte);
            current().setTableImage(matOfByte.toArray());
            outputImage.release();

        } catch (DrawerException e) {
            LOGGER.error("Cannot prepere the view image.", e);
        } finally {
            return this;
        }
    }
}
