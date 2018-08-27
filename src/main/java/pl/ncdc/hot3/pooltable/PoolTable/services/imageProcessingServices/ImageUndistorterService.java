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
        cameraMatrix.put(0, 0, 1755.73196841084);
        cameraMatrix.put(0, 2, 1024);
        cameraMatrix.put(1, 1, 1747.254824521836);
        cameraMatrix.put(1, 2, 768);

        distCoeffs.put(0, 0, -0.4001622593334911);
        distCoeffs.put(1, 0, 0.1676437868703358);

        /*quadrangleSrc.put(0,0, 63.84375 * scaleX);
        quadrangleSrc.put(1, 0, 839.84375 * scaleX);
        quadrangleSrc.put(2, 0, 72.84375 * scaleX);
        quadrangleSrc.put(3, 0, 846.84375 * scaleX);

        quadrangleSrc.put(0,1,145.40625 * scaleY);
        quadrangleSrc.put(1,1,147.40625 * scaleY);
        quadrangleSrc.put(2,1,542.40625 * scaleY);
        quadrangleSrc.put(3,1,526.40625 * scaleY);

        quadrangleDst.put(0,0, 68.0);
        quadrangleDst.put(1,0, 843.0);
        quadrangleDst.put(2,0, 68.0);
        quadrangleDst.put(3,0, 843.0);

        quadrangleSrc.put(0, 1, 146.0);
        quadrangleSrc.put(1,1, 146.0);
        quadrangleSrc.put(2,1, 534.0);
        quadrangleSrc.put(3,1, 534.0);*/

        srcPoints = new Point[] {
                new Point(177.0, 349.0),
                new Point(1942.0, 340.0),
                new Point(1963.0, 1225.0),
                new Point(176.0, 1255.0)
        };


        dstPoints = new Point[] {
                new Point(130.0, 365.0),
                new Point(1915.0, 365.0),
                new Point(1915.0, 1250.0),
                new Point(130.0, 1250.0)
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
            undistorted = new Mat();
            Imgproc.warpPerspective(distorted, undistorted, transformPerspective, new Size(2048,1536));
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