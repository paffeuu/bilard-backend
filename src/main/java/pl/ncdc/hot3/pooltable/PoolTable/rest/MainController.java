package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.apache.catalina.mapper.Mapper;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
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
    @GetMapping("/get-pool-table")
    public ResponseEntity<PoolTable> test() throws CameraServiceException {
        System.gc();
        PoolTable table = tableStoryService
                .next()
                .save(12)
                .findBalls()
                .findCue()
                .makePredictions()
                .showPrevious()
                .save(12)
                .build();

        return ResponseEntity.ok(table);

    }

    @PutMapping("/get-pool-table")
    public ResponseEntity<Properties> setProperties(@RequestParam Properties properties){
//        Mapper mapper = new DozerBeanMapper();
        return ResponseEntity.ok(this.properties);
    }


    @RequestMapping(value = "/set-visible", method = RequestMethod.GET)
    public void setShowPrevious() {
        properties.setShowPreviousPosition(!properties.isShowPreviousPosition());
    }

}
