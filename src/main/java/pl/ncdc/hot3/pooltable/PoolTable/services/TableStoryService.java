package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.MockupService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;

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

    private PreviousPositionService previousPositionService;

    @Autowired
    public TableStoryService(
            Detector detector,
            CameraService cameraService,
            Drawer drawer,
            Properties properties,
            PreviousPositionService previousPositionService
    ) {
        this.detector = detector;
        this.cameraService = cameraService;
        this.drawer = drawer;
        this.properties = properties;
        this.previousPositionService = previousPositionService;

        currentTableIndex = -1;

        tableStory = new ArrayList<>();
    }

    public TableStoryService saveBefore(int framesStep) {
        if (OpenCVBufforFlushService.getCounter() % framesStep == 0 && (outputImage != null || !outputImage.empty())) {
            makeView();
            Mat outputClone = outputImage.clone();
            Imgcodecs.imwrite("before_frame" + counter + ".jpg", outputClone);
        }
        return this;
    }

    public TableStoryService saveAfter(int framesStep) {
        if (OpenCVBufforFlushService.getCounter() % framesStep == 0 && (outputImage != null || !outputImage.empty())) {
            makeView();
            Mat outputClone = outputImage.clone();
            Imgcodecs.imwrite("after_frame" + counter + ".jpg", outputClone);
        }
        return this;
    }

    private PoolTable current(int backwardStep){
        if (currentTableIndex >= backwardStep)
            return tableStory.get((currentTableIndex - backwardStep) % LIMIT_OF_TABLES);

        return new PoolTable();
    }

    private PoolTable current(){
        if (currentTableIndex >= 0)
            return tableStory.get(currentTableIndex % LIMIT_OF_TABLES);

        return new PoolTable();
    }


    public TableStoryService next() {
        try {
            outputImage = cameraService.getSnap();
            detector.setSourceImg(outputImage.clone());
        } catch (CameraServiceException e) {
            LOGGER.warn("Camera view not available. Empty table image as a source");
            outputImage = detector.getSourceImg().clone();
        }


        if (++currentTableIndex > 1)
            current(2).setTableImage(null);
        tableStory.add(new PoolTable());

        counter++;




        return this;
    }

    public TableStoryService findCue(){
        try {
            Line cue = detector.findStickLine();
            current().setCue(cue);
        } catch (MissingCueLineException e) {
            //LOGGER.info("Cue not founded.");
        } catch (LineServiceException e) {
            LOGGER.warn("Missing line when try find cue.", e);
        } catch (DetectorException e) {
            LOGGER.error("Error while trying find cue.", e);
        }
        return this;
    }

    public TableStoryService makePredictions(){

        try {
            current().setPredictions(detector.getPredictions(current().getCue()));
        } catch (CueServiceException e) {
            //LOGGER.warn("Predictions canceled.", e);
        } catch (LineServiceException e) {
            LOGGER.error("Cannot extend predicted line.", e);
        }
        return this;

    }

    public TableStoryService findBalls() {
        try {
            ArrayList<Ball> q = detector.createListOfBalls();
            current().setBalls(q);
        } catch (BallsDetectorException e) {
            LOGGER.info("Can not find balls");
        }
        return this;
    }

    public PoolTable build() {
        return  makeView()
                .current();
    }

    public TableStoryService showPrevious(){

        if (properties.isShowPreviousPosition()) {
            saveToPrevService();

            try {
                drawer.drawBalls(outputImage, previousPositionService.getPreviousPosition(), new Scalar(255, 0, 255));
            } catch (DrawerException e) {
                LOGGER.warn("Could not draw previous balls possition. Nested: " + e);
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
            LOGGER.warn("Can not save previous position. Nested: ", e.getMessage());
        }
    }

    private TableStoryService makeView(){
        try {
            drawer.draw(outputImage, current().getCue(), current().getBalls(), current().getPredictions());

            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", outputImage, matOfByte);
            current().setTableImage(matOfByte.toArray());
        } catch (DrawerException e) {
            LOGGER.error("Cannot prepere the view image.", e);
        } finally {
            return this;
        }
    }

}
