package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.services.*;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.SnapshotGetterService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping(path = "/pooltable")
public class MainController {
    @Autowired
    private SnapshotGetterService snapshotGetterService;
    @Autowired
    private ImageUndistorterService undistorter;
    @Autowired
    private Drawer drawer;
    @Autowired
    private CueService cueService;
    @Autowired
    private Detector detector;
    @Autowired
    private PreviousPositionService previousPositionService;

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/get-pool-table", method = RequestMethod.GET)
    public ResponseEntity<PoolTable> test() throws CueServiceException, DetectorException, LineServiceException {
        PoolTable table = new PoolTable();
        MatOfByte matOfByte = new MatOfByte();
        Mat result = undistorter.undistort(OpenCVBufforFlushService.getLastFrame());

        detector.setSourceImg(result.clone());
        table.setBalls(detector.createListOfBalls());

        if (OpenCVBufforFlushService.getCounter() % 4 == 0) {
            previousPositionService.addPosition(table.getBalls());
            previousPositionService.findLastStillPosition();
        }

        List<Line> cueAndPredictions = cueService.getStickWithPredictions(4);


        if ( !cueAndPredictions.isEmpty() ) {
            table.setCue(cueAndPredictions.get(0));

            drawer.draw(result, cueAndPredictions, table.getBalls());
        }

        if (previousPositionService.isShowPrevious()) {
            if (previousPositionService.getPreviousPosition() != null) {
                drawer.drawBalls(result, previousPositionService.getPreviousPosition(), new Scalar(255, 0, 255));
            }
        }
        Imgcodecs.imencode(".jpg", result, matOfByte);
        table.setTableImage(matOfByte.toArray());
        return ResponseEntity.ok(table);

    }

    @RequestMapping(value = "/set-visible", method = RequestMethod.GET)
    public void setShowPrevious() {
        previousPositionService.setShowPrevious(!previousPositionService.isShowPrevious());
    }
}
