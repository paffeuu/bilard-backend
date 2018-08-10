package pl.ncdc.hot3.pooltable.PoolTable.rest;

import pl.ncdc.hot3.pooltable.PoolTable.services.Drawer;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.SnapshotGetterService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;


@RestController
@RequestMapping(path="/pooltable")
public class MainController {
    @Autowired
    private SnapshotGetterService snap;
    @Autowired
    private ImageUndistorterService undistorter;
    @Autowired
    private Drawer drawer;
    @Autowired
    private Detector detector;

    @CrossOrigin(origins = "http://localhost:4200")

    @RequestMapping(value = "/get-snapshot", method = RequestMethod.GET)
    public ResponseEntity<PoolTable> getPoolTableImage() throws DetectorException {
        Mat undistorted = undistorter.undistort(snap.getLiveSnapshot());

        MatOfByte matOfByte = new MatOfByte();

        PoolTable table = new PoolTable();

        detector.setSourceImg(undistorted.clone());
        table.setBalls(detector.createListOfBalls(undistorted));
        table.setCue(detector.findStickLine());
        drawer.draw(undistorted, table.getCue());
        Imgcodecs.imencode(".jpg", undistorted, matOfByte);
        table.setTableImage(matOfByte.toArray());

        return ResponseEntity.ok(table);
    }

    @GetMapping("/test")
    public PoolTable test(){
        return new PoolTable();
    }
}
