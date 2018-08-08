package pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import pooltable.exceptions.DetectorException;

public class DetectorTest {

    @Test(expected = DetectorException.class)
    public void getLinesShouldThrowExceptionWhenSourceNull() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

        detector.setSourceImg(null);
        detector.findStickLine();
    }

    @Test
    public void getStickShouldReturnDetectedStickFor2SourceViews() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = ProjectProperties.BASE_PATH + "Photo1.png";
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2.png";

        Mat sourceImg = Imgcodecs.imread(fileUrl, Imgcodecs.IMREAD_GRAYSCALE);
        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_GRAYSCALE);

        Detector detector = new Detector();

        detector.setSourceImg(sourceImg);
        detector.saveStaticLines();

        detector.setSourceImg(sourceWithStickImg);
        Line line = detector.findStickLine();

        Assert.assertNotNull(line);
        System.out.println(line.toString());
    }

}
