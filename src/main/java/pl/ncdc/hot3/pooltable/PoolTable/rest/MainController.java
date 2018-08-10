package pl.ncdc.hot3.pooltable.PoolTable.rest;

import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.services.Drawer;
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
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;


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
    private Detector detector;

    @CrossOrigin(origins = "http://localhost:4200")

    @RequestMapping(value = "/get-snapshot", method = RequestMethod.GET)
    public ResponseEntity<PoolTable> getPoolTableImage() throws DetectorException {
        PoolTable table = new PoolTable();

        if (!snapshotGetterService.isOpening()) {
            Mat in = snapshotGetterService.getLiveSnapshot();
            if (in != null && !in.empty()) {
                MatOfByte matOfByte = new MatOfByte();
                try {
                    Mat result = undistorter.undistort(in);
                    detector.setSourceImg(result.clone());
                    table.setBalls(detector.createListOfBalls(result.clone()));
                    table.setCue(detector.findStickLine());
                    Line line = detector.getExtendedStickLine(table.getCue());
                    drawer.draw(result, line);
                    Imgcodecs.imencode(".jpg", result, matOfByte);
                    table.setTableImage(matOfByte.toArray());
                    return ResponseEntity.ok(table);

                } catch (Exception e) {
                    System.out.println("error");
                    return ResponseEntity.ok(table);
                }
            } else {
                snapshotGetterService.reset();
                System.out.println("in empty or null");
                return ResponseEntity.ok(table);
            }
        } else {
            System.out.println("is opening");
            return ResponseEntity.ok(table);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/get-pool-table", method = RequestMethod.GET)
    public ResponseEntity<PoolTable> test() {
        PoolTable table = new PoolTable();
        MatOfByte matOfByte = new MatOfByte();
        Mat result = undistorter.undistort(OpenCVBufforFlushService.getLastFrame());
        try {
            detector.setSourceImg(result.clone());
            table.setBalls(detector.createListOfBalls(result.clone()));
            table.setCue(detector.findStickLine());
            Line line = detector.getExtendedStickLine(table.getCue());
            drawer.draw(result, line);
            Imgcodecs.imencode(".jpg", result, matOfByte);
            table.setTableImage(matOfByte.toArray());
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("wyslane");
        //OpenCVBufforFlushService.setIsNotNeeded(true);
        return ResponseEntity.ok(table);
    }
}
