package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.Drawer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Detector.class, CueService.class, Properties.class})
public class DetectorTests {

    @Autowired
    private Detector detector;

    @Autowired
    private CueService cueService;

    @Autowired
    private Properties properties;


    @Test
    public void Test() throws CueServiceException {
        Line line = new Line(new Point(0, 0), new Point(20, 20));
        Line extended = cueService.getExtendedStickLineForOneSide(line);

        Assert.assertNotNull(extended);
        Assert.assertNotNull(extended.getBegin());
        Assert.assertNotNull(extended.getEnd());
    }

    String BASE_PATH = "src/main/resources/";

    @Before
    public void init() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test(expected = DetectorException.class)
    public void getLinesShouldThrowExceptionWhenSourceNull() throws DetectorException, CueServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

        detector.setSourceImg(null);
        detector.findStickLine();
    }

    @Test
    public void getStickShouldReturnDetectedStickForImageWithExtraLine() throws DetectorException, CueServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = BASE_PATH + "jacek1.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        //Detector detector = new Detector();

        detector.setSourceImg(sourceWithStickImg);
        Line line = detector.findStickLine();

        Assert.assertNotNull(line);
    }

    @Test
    public void getExtendedStickShouldReturnExtendedLineForInputStick() throws DetectorException, CueServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl2 = BASE_PATH + "jacek1.png";

        Mat sourceWithStickImg = Imgcodecs.imread(fileUrl2, Imgcodecs.IMREAD_COLOR);

        Detector detector = new Detector();
        CueService cueService = new CueService(new Properties(), detector);

        detector.setSourceImg(sourceWithStickImg);
        Line line = detector.findStickLine();

        Line extendedLine = null;

        if (line != null){
            extendedLine = cueService.getExtendedStickLineForBothSides(line);
        }else
            System.out.println("Linia jest nullem");

        Assert.assertNotNull(extendedLine);

        Mat sourceClone = detector.getSourceImg().clone();
        Imgproc.line(sourceClone, extendedLine.getBegin(), extendedLine.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgcodecs.imwrite("line.png", sourceWithStickImg);
        Imgcodecs.imwrite("extendedLine.png", sourceClone);

    }

    @Test
    public void saveStaticLinesShouldFindOneMoreLineForSecondSource() throws DetectorException, CueServiceException {
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
    public void shouldFindLinesForMultipleSourcesAndSaveForPreview() throws DetectorException, CueServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = BASE_PATH + "jacek" ;

        Detector detector = new Detector();
        Drawer drawer = new Drawer();

        for (int i = 0; i < 9; i++){
            String file = fileUrl + (i+1) + ".png";
            String fileTemp = fileUrl + (i+1) + "-temp.png";

            Mat sourceWithStickImg = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

            detector.setSourceImg(sourceWithStickImg.clone());
            Line line = detector.findStickLine();

            Imgproc.line(sourceWithStickImg, line.getBegin(), line.getEnd(), new Scalar(110, 0, 255), 9);

            Mat dst = drawer.drawBandLines(sourceWithStickImg);
            Imgcodecs.imwrite(fileTemp, dst);
        }
    }

    @Test
    public void checkIfPointIsInsideTheBands(){
        Point point = new Point(1000, 1000);

        boolean isTrue = Detector.isPointInsideBand(point, properties);

        Assert.assertTrue(isTrue);
    }

}
