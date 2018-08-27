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
        //2K
        /*cameraMatrix.put(2, 2, 1);
        cameraMatrix.put(0, 0, 1755.73196841084);
        cameraMatrix.put(0, 2, 1024);
        cameraMatrix.put(1, 1, 1747.254824521836);
        cameraMatrix.put(1, 2, 768);

        distCoeffs.put(0, 0, -0.4001622593334911);
        distCoeffs.put(1, 0, 0.1676437868703358);*/

        //HD
        cameraMatrix.put(2, 2, 1);
        cameraMatrix.put(0, 0, 991.4262945972393);
        cameraMatrix.put(0, 2, 640);
        cameraMatrix.put(1, 1, 993.9357197471496);
        cameraMatrix.put(1, 2, 360);

        distCoeffs.put(0, 0, -0.4110309525718729);
        distCoeffs.put(1, 0, 0.2250083648489881);

        double hdTo2kRatioX = 2048.0 / 1280.0;
        double hdTo2kRatioY = 1536.0 / 720.0;

        srcPoints = new Point[] {
                new Point(177.0 / hdTo2kRatioX, 349.0 / hdTo2kRatioY),
                new Point(1942.0 / hdTo2kRatioX, 340.0 / hdTo2kRatioY),
                new Point(1963.0 / hdTo2kRatioX, 1225.0 / hdTo2kRatioY),
                new Point(176.0 / hdTo2kRatioX, 1255.0 / hdTo2kRatioY)
        };


        dstPoints = new Point[] {
                new Point(130.0 / hdTo2kRatioX, 365.0 / hdTo2kRatioY),
                new Point(1915.0 / hdTo2kRatioX, 365.0 / hdTo2kRatioY),
                new Point(1915.0 / hdTo2kRatioX, 1250.0 / hdTo2kRatioY),
                new Point(130.0 / hdTo2kRatioX, 1250.0 / hdTo2kRatioY)
        };

        MatOfPoint2f src = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dst = new MatOfPoint2f(dstPoints);

        transformPerspective = Imgproc.getPerspectiveTransform(src, dst);
    }

    public Mat undistort(Mat distorted) {
        try {
            Mat undistorted = new Mat();
            Imgproc.undistort(distorted, undistorted, cameraMatrix, distCoeffs);
            //distorted = undistorted.clone();
            //undistorted = new Mat();
            //Imgproc.warpPerspective(distorted, undistorted, transformPerspective, new Size(2048,1536));
            for (int i = 0; i < dstPoints.length; i++) {
                //Imgproc.line(undistorted, dstPoints[i], dstPoints[(i+1)%dstPoints.length], new Scalar(255,255,100));
                Imgproc.line(undistorted, srcPoints[i], srcPoints[(i+1)%dstPoints.length], new Scalar(0,255,255), 1);
            }
            return undistorted;
        } catch (Exception e) {
            return null;
        }
    }
}