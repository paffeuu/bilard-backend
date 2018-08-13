package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;

public class DetectorTest {

    String BASE_PATH = "src/main/resources/";


    @Test(expected = DetectorException.class)
    public void getLinesShouldThrowExceptionWhenSourceNull() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

        detector.setSourceImg(null);
        detector.findStickLine();
    }

    @Test
    public void getStickShouldReturnDetectedStickForImageWithExtraLine() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = BASE_PATH + "jacek1.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        detector.setSourceImg(sourceWithStickImg);
        Line line = detector.findStickLine();

        Assert.assertNotNull(line);
    }

    @Test
    public void getExtendedStickShouldReturnExtendedLineForInputStick() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = BASE_PATH + "jacek1.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();

        detector.setSourceImg(sourceWithStickImg);
        Line line = detector.findStickLine();

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
        String fileUrl2 = BASE_PATH + "jacek1.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);
        Mat cleanImg = sourceWithStickImg.clone();

        Detector detector = new Detector();

        detector.setSourceImg(sourceWithStickImg);
        Line line = detector.findStickLine();

        Imgproc.line(cleanImg, line.getBegin(), line.getEnd(), new Scalar(0, 0, 255), 5);

        System.out.println(line.toString());
        Assert.assertNotNull(line);

    }

    @Test
    public void shouldExtendLinesForMultipleSourcesAndSaveForPreview() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = BASE_PATH + "jacek" ;

        Detector detector = new Detector();

        for (int i = 0; i < 9; i++){
            String file = fileUrl + (i+1) + ".png";
            String fileTemp = fileUrl + (i+1) + "-temp.png";

            Mat sourceWithStickImg = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

            detector.setSourceImg(sourceWithStickImg.clone());
            Line line = detector.findStickLine();
            Line extendedLine = detector.getExtendedStickLine(line);

            Imgproc.line(sourceWithStickImg, line.getBegin(), line.getEnd(), new Scalar(0, 255, 0), 5);
            Imgproc.line(sourceWithStickImg, extendedLine.getBegin(), extendedLine.getEnd(), new Scalar(0, 0, 255), 5);
            Imgproc.line(sourceWithStickImg, line.getBegin(), line.getEnd(), new Scalar(0, 255, 0), 5);

            Mat dst = detector.drawBandLines(sourceWithStickImg);
            Imgcodecs.imwrite(fileTemp, dst);
        }
    }

}
