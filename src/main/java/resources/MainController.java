package resources;

import imageProcessingServices.ImageUndistorterService;
import imageProcessingServices.SnapshotGetterService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pooltable.PoolTable.Detector;
import pooltable.PoolTable.model.PoolTable;
import pooltable.exceptions.DetectorException;


@Controller
@RequestMapping(path="/pooltable")

public class MainController {
    @Autowired
    private SnapshotGetterService snap;
    @Autowired
    private ImageUndistorterService undistorter;

    @Autowired
    private Detector detector;

    @CrossOrigin(origins = "http://localhost:4200")

    @RequestMapping(produces = MediaType.IMAGE_JPEG_VALUE, value = "/get-snapshot", method = RequestMethod.GET)
    public ResponseEntity<PoolTable> getPoolTableImage() throws DetectorException {
        Mat undistorted = undistorter.undistort(snap.getLiveSnapshot());

        MatOfByte matOfByte = new MatOfByte();

        Imgcodecs.imencode(".jpg", undistorted, matOfByte);

        PoolTable table = new PoolTable();

        detector.setSourceImg(undistorted);
        table.setBalls(detector.createListOfBalls());
        table.setCue(detector.findStickLine());
        table.setTableImage(matOfByte.toArray());

        return ResponseEntity.ok(table);
    }
}
