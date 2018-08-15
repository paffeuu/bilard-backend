package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
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
    private Properties properties;

    @Autowired
    private TableStoryService tableStoryService;

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/get-pool-table", method = RequestMethod.GET)
    public ResponseEntity<PoolTable> test() {

        PoolTable table = tableStoryService
                .next()
                .findBalls()
                .findCue()
                .makePredictions()
                .showPrevious()
                .build();

        return ResponseEntity.ok(table);

    }

    @RequestMapping(value = "/set-visible", method = RequestMethod.GET)
    public void setShowPrevious() {
        properties.setShowPreviousPosition(true);
    }
}
