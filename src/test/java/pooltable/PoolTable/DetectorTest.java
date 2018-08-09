package pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pooltable.PoolTable.model.Line;
import pooltable.exceptions.DetectorException;

public class DetectorTest {

    @Test(expected = DetectorException.class)
    public void getLinesShouldThrowExceptionWhenSourceNull() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

        detector.setSourceImg(null);
        detector.findStickLine(detector.getSourceImg());
    }

    @Test
    public void getStickShouldReturnDetectedStickForImageWithExtraLine() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2a.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        Line line = detector.findStickLine(sourceWithStickImg);

        Assert.assertNotNull(line);
    }

    @Test
    public void getExtendedStickShouldReturnExtendedLineForInputStick() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2d.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        Line line = detector.findStickLine(sourceWithStickImg);

        Line extendedLine = null;

        if (line != null){
            extendedLine = detector.getExtendedStickLine(line);
        }else
            System.out.println("Linia jest nullem");

        Assert.assertNotNull(extendedLine);

        Mat sourceClone = detector.getSourceImg().clone();
        Imgproc.line(sourceClone, extendedLine.getBegin(), extendedLine.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgcodecs.imwrite("line.png", sourceWithStickImg);
        Imgcodecs.imwrite("extendedLine.png", sourceClone);

    }

    @Test
    public void saveStaticLinesShouldFindOneMoreLineForSecondSource() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = ProjectProperties.BASE_PATH + "Photo2d.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        Line line = detector.findStickLine(sourceWithStickImg);

        Assert.assertNotNull(line);

    }

}
