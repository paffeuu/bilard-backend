package pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pooltable.exceptions.DetectorException;

import java.util.ArrayList;

public class DetectorTest {

    @Test(expected = DetectorException.class)
    public void getLinesShouldThrowExceptionWhenSourceNull() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

        detector.setSourceImg(null);
        detector.findStickLine(detector.getSourceImg());
    }

    @Test
    public void getStickShouldReturnDetectedStickFor2SourceViews() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = ProjectProperties.BASE_PATH + "Photo1.png";
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2a.png";

        Mat sourceImg = Imgcodecs.imread(fileUrl, Imgcodecs.IMREAD_COLOR);
        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        detector.setSourceImg(sourceImg);
        detector.saveStaticImage();

        detector.setSourceImg(sourceWithStickImg);

        Line line = detector.findStickLine(detector.getSourceImg());

        Assert.assertNotNull(line);
    }

    @Test
    public void getExtendedStickShouldReturnLineForInputStick() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = ProjectProperties.BASE_PATH + "Photo1.png";
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2d.png";

        Mat sourceImg = Imgcodecs.imread(fileUrl, Imgcodecs.IMREAD_COLOR);
        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        detector.setSourceImg(sourceImg);
        detector.saveStaticImage();

        Line line = detector.findStickLine(sourceWithStickImg);

        Line extendedLine = null;

        if (line != null){
            extendedLine = detector.getExtendedStickLine(line);
        }else
            System.out.println("Linia jest nullem");

        Assert.assertNotNull(extendedLine);

        Mat sourceClone = sourceImg.clone();
        Imgproc.line(sourceClone, extendedLine.getBegin(), extendedLine.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgcodecs.imwrite("line.png", sourceWithStickImg);
        Imgcodecs.imwrite("extendedLine.png", sourceClone);

    }

    @Test
    public void saveStaticLinesShouldFindOneMoreLineForSecondSource() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = ProjectProperties.BASE_PATH + "Photo1.png";
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2d.png";

        Mat sourceImg = Imgcodecs.imread(fileUrl, Imgcodecs.IMREAD_COLOR);
        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        detector.setSourceImg(sourceImg);
        detector.saveStaticImage();

        Line line = detector.findStickLine(sourceWithStickImg);

        Assert.assertNotNull(line);

    }

}
