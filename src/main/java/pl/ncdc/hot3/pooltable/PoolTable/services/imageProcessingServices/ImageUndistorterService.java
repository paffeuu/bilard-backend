package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@Service
public class ImageUndistorterService {
    private Properties properties;
    private Mat cameraMatrix;
    private Mat distCoeffs;

    @Autowired
    public ImageUndistorterService(Properties properties) {
        this.properties = properties;
        this.cameraMatrix = properties.getCameraMatrix();
        this.distCoeffs = properties.getDistCoeffs();
    }

    public Mat undistort(Mat distorted) {
        try {
            Mat undistorted = new Mat();
            Imgproc.undistort(distorted, undistorted, cameraMatrix, distCoeffs);
            return undistorted;
        } catch (Exception e) {
            return null;
        }
    }
}