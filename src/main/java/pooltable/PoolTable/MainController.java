package pooltable.PoolTable;

import imageProcessingServices.ImageUndistorterService;
import imageProcessingServices.SnapshotGetterService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;




@Controller
@RequestMapping(path="/pooltable")

public class MainController {
    @Autowired
    private SnapshotGetterService snap;
    @Autowired
    private ImageUndistorterService undistorter;

    @CrossOrigin(origins = "http://localhost:4200")

    @RequestMapping(produces = MediaType.IMAGE_JPEG_VALUE, value = "/get-snapshot", method = RequestMethod.GET)
    public @ResponseBody byte[] getPoolTableImage() throws IOException {
        Mat in = snap.getLiveSnapshot();
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", undistorter.undistort(in), matOfByte);
        return matOfByte.toArray();
    }
}
