package pooltable.PoolTable;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageUndistorter {
    private Mat cameraMatrix = Mat.zeros(3, 3, CvType.CV_64F);
    private Mat distCoeffs = Mat.zeros(8, 1, CvType.CV_64F);
    private Mat undistorted = new Mat();
    public ImageUndistorter() {
        cameraMatrix.put(2, 2, 1);
        cameraMatrix.put(0, 0, 1755.73196841084);
        cameraMatrix.put(0, 2, 1024);
        cameraMatrix.put(1, 1, 1747.254824521836);
        cameraMatrix.put(1, 2, 768);

        distCoeffs.put(0, 0, -0.4001622593334911);
        distCoeffs.put(1, 0, 0.1676437868703358);
    }

    public Mat undistort(Mat distorted) {
        Imgproc.undistort(distorted, undistorted, cameraMatrix, distCoeffs);
        return this.undistorted;
    }
}
