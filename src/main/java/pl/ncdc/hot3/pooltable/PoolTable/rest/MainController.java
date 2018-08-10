package pl.ncdc.hot3.pooltable.PoolTable.rest;

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
                    System.out.println("before undsitort");
                    Mat result = undistorter.undistort(in);
                    System.out.println("after undi");
                    //detector.setSourceImg(result.clone());
                    //table.setBalls(detector.createListOfBalls());
                    //table.setCue(detector.findStickLine());
                    System.out.println("after detect");

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
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public PoolTable test() {
        PoolTable p = new PoolTable();
        MatOfByte m = new MatOfByte();
        try {
            //Imgcodecs.imwrite("test.jpg", OpenCVBufforFlushService.getLastFrame());
            Imgcodecs.imencode(".jpg", OpenCVBufforFlushService.getLastFrame(), m);
            p.setTableImage(m.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("wyslane");
        //OpenCVBufforFlushService.setIsNotNeeded(true);
        return p;
    }
}
