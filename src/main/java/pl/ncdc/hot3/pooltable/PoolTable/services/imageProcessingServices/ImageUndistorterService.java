package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.ConfigurableProperties;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@Service
public class ImageUndistorterService {
    private ConfigurableProperties configurableProperties;
    private Properties properties;
    private Mat cameraMatrix;
    private Mat distCoeffs;
    private Mat transformPerspective;
    private Mat transformProjectorPerspective;


    @Autowired
    public ImageUndistorterService(
            Properties properties,
            ConfigurableProperties configurableProperties
    ) {
        this.properties = properties;
        this.configurableProperties = configurableProperties;
        this.cameraMatrix = properties.getCameraMatrix();
        this.distCoeffs = properties.getDistCoeffs();
    }

    public Mat undistort(Mat distorted) {
        try {
            Mat initiallyUndistorted = new Mat();
            Imgproc.undistort(distorted, initiallyUndistorted, properties.getCameraMatrix(), properties.getDistCoeffs());
            distorted.release();
            Mat undistorted = new Mat();
            prepareUndistortingMats();
            Imgproc.warpPerspective(initiallyUndistorted, undistorted, transformPerspective, new Size(1280,720));
            initiallyUndistorted.release();
            return undistorted;
        } catch (Exception e) {
            return null;
        }
    }

    private void prepareUndistortingMats() {
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

    private void prepareProjectorUndistortingMats() {
        Point[] dstPoints = new Point[] {
                configurableProperties.getProjectorLeftTopCorner(),
                configurableProperties.getProjectorRightTopCorner(),
                configurableProperties.getProjectorRightBottomCorner(),
                configurableProperties.getProjectorLeftBottomCorner()
        };

        Point[] srcPoints = new Point[] {
                new Point(properties.getTableBandLeft(), properties.getTableBandTop()),
                new Point(properties.getTableBandRight(), properties.getTableBandTop()),
                new Point(properties.getTableBandRight(), properties.getTableBandBottom()),
                new Point(properties.getTableBandLeft(), properties.getTableBandBottom())
        };

        MatOfPoint2f src = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dst = new MatOfPoint2f(dstPoints);

        transformProjectorPerspective = Imgproc.getPerspectiveTransform(src, dst);
    }

    public Mat projectorWarp(Mat src) {
        Mat dst = new Mat();

        this.prepareProjectorUndistortingMats();
        Imgproc.warpPerspective(src, dst, transformProjectorPerspective,
                new Size(properties.getImageSourceWidth(),properties.getImageSourceHeight()));

        return dst;
    }

}