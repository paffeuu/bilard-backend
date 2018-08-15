package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.MissingCueLineException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;

import java.util.ArrayList;
import java.util.List;

@Service
public class TableStoryService {

    final static int LIMIT_OF_TABLES = 512;
    final static Logger LOGGER = LoggerFactory.getLogger(TableStoryService.class);

    private int currentTableIndex;
    private List<PoolTable> tableStory;
    private Mat outputImage;

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

        currentTableIndex = 0;

        tableStory = new ArrayList<>();
    }

    private PoolTable current(){
        return tableStory.get(currentTableIndex % LIMIT_OF_TABLES);
    }

    private void clearOldTableImage(){
        if (tableStory.size() >= 1){
            current().setTableImage(null);
        }
    }

    public TableStoryService next(){
        clearOldTableImage();
        tableStory.add(new PoolTable());
        currentTableIndex++;

        outputImage = cameraService.getSnap();
        detector.setSourceImg(outputImage.clone());

        return this;
    }

    public TableStoryService findCue(){
        try {
            Line cue = detector.findStickLine();
            current().setCue(cue);
        } catch (MissingCueLineException e) {
            LOGGER.info("Cue not founded.");
        } catch (DetectorException e) {
            LOGGER.error("Error while trying find cue.", e);
        } catch (LineServiceException e) {
            LOGGER.warn("Missing line when try find cue.");
        }
        return this;
    }

    public TableStoryService makePredictions(){

        try {
            current().setPredictions(detector.getPredictions());
        } catch (CueServiceException e) {
            LOGGER.warn("Predictions canceled.", e);
        } catch (LineServiceException e) {
            LOGGER.error("Cannot extend predicted line.", e);
        }
        return this;

    }

    public TableStoryService findBalls() {
        current().setBalls(detector.createListOfBalls());
        return this;
    }

    public PoolTable build() {
        return  makeView()
                .current();
    }

    public TableStoryService showPrevious(){
        saveToPrevService();

        if (properties.isShowPreviousPosition()) {
            if (previousPositionService.getPreviousPosition() != null) {
                drawer.drawBalls(outputImage, previousPositionService.getPreviousPosition(), new Scalar(255, 0, 255));
            }
        }

        return this;
    }

    private void saveToPrevService(){
        if (OpenCVBufforFlushService.getCounter() % 4 == 0) {
            previousPositionService.addPosition(current().getBalls());
            previousPositionService.findLastStillPosition();
        }
    }

    private TableStoryService makeView(){
        drawer.drawLine(outputImage, tableStory.get(currentTableIndex).getCue());
        drawer.draw(outputImage, tableStory.get(currentTableIndex).getPredictions(), tableStory.get(currentTableIndex).getBalls());

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", outputImage, matOfByte);
        current().setTableImage(matOfByte.toArray());

        return this;
    }

}
