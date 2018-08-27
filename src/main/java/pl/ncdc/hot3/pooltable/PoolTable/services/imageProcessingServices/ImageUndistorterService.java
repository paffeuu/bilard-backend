package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageUndistorterService {
    private Mat cameraMatrix = Mat.zeros(3, 3, CvType.CV_64F);
    private Mat distCoeffs = Mat.zeros(8, 1, CvType.CV_64F);
    private Mat quadrangleSrc = Mat.zeros(4,2, CvType.CV_32FC2);
    private Mat quadrangleDst = Mat.zeros(4,2,CvType.CV_32FC2);
    private Mat transformPerspective;
    private Point[] dstPoints;
    private Point[] srcPoints;

    @Autowired
    public ImageUndistorterService() {
        cameraMatrix.put(2, 2, 1);
        cameraMatrix.put(0, 0, 991.4262945972393);
        cameraMatrix.put(0, 2, 640);
        cameraMatrix.put(1, 1, 993.9357197471496);
        cameraMatrix.put(1, 2, 360);

        distCoeffs.put(0, 0, -0.4110309525718729);
        distCoeffs.put(1, 0, 0.2250083648489881);

        srcPoints = new Point[] {
                new Point(151.0, 128.0),
                new Point(1131.0, 141.0),
                new Point(1133.0, 628.0),
                new Point(142.5, 631.0)
        };

        dstPoints = new Point[] {
                new Point(148.0, 135.0),
                new Point(1131.0, 135.0),
                new Point(1131.0, 630.0),
                new Point(148.0, 630.0)
        };

        MatOfPoint2f src = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dst = new MatOfPoint2f(dstPoints);

        transformPerspective = Imgproc.getPerspectiveTransform(src, dst);
    }

    public Mat undistort(Mat distorted) {
        try {
            Mat undistorted = new Mat();
            Imgproc.undistort(distorted, undistorted, cameraMatrix, distCoeffs);
            distorted = undistorted.clone();
            undistorted.release();
            Imgproc.warpPerspective(distorted, undistorted, transformPerspective, new Size(1280,720));
            for (int i = 0; i < dstPoints.length; i++) {
                Imgproc.line(undistorted, dstPoints[i], dstPoints[(i+1)%dstPoints.length], new Scalar(255,255,100));
                //Imgproc.line(undistorted, srcPoints[i], srcPoints[(i+1)%dstPoints.length], new Scalar(0,255,255), 1);
            }
            return undistorted;
        } catch (Exception e) {
            return null;
        }
    }
}