package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@Service
public class ImageUndistorterService {
    private Mat cameraMatrix = Mat.zeros(3, 3, CvType.CV_64F);
    private Mat distCoeffs = Mat.zeros(8, 1, CvType.CV_64F);
    private Mat transformPerspective;
    private Properties properties;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUndistorterService.class);

    @Autowired
    public ImageUndistorterService(
            Properties properties
    ) {
        this.properties = properties;
        prepareUndistortingMats();
    }

    private void prepareUndistortingMats() {
        cameraMatrix.put(2, 2, 1);
        cameraMatrix.put(0, 0, 991.4262945972393);
        cameraMatrix.put(0, 2, 640);
        cameraMatrix.put(1, 1, 993.9357197471496);
        cameraMatrix.put(1, 2, 360);

        distCoeffs.put(0, 0, -0.4110309525718729);
        distCoeffs.put(1, 0, 0.2250083648489881);

        Point[] srcPoints = new Point[] {
                properties.getImproperLeftTopCorner(),
                properties.getImproperRightTopCorner(),
                properties.getImproperRightBottomCorner(),
                properties.getImproperLeftBottomCorner()
        };

        Point[] dstPoints = new Point[] {
                new Point(properties.getTableBandLeft(), properties.getTableBandTop()),
                new Point(properties.getTableBandRight(), properties.getTableBandTop()),
                new Point(properties.getTableBandRight(), properties.getTableBandBottom()),
                new Point(properties.getTableBandLeft(), properties.getTableBandBottom())
        };

        MatOfPoint2f src = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dst = new MatOfPoint2f(dstPoints);

        transformPerspective = Imgproc.getPerspectiveTransform(src, dst);
    }

    public Mat undistort(Mat distorted) {
        try {
            Mat initiallyUndistorted = new Mat();
            Imgproc.undistort(distorted, initiallyUndistorted, cameraMatrix, distCoeffs);
            distorted.release();
            Mat undistorted = new Mat();
            Imgproc.warpPerspective(initiallyUndistorted, undistorted, transformPerspective,
                    new Size(properties.getImageSourceWidth(),properties.getImageSourceHeight()));
            return undistorted;
        } catch (Exception e) {
            LOGGER.error("Image could not have been properly undistorted.");
            return null;
        }
    }
}