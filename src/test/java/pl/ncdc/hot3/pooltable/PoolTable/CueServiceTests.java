package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.Drawer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CueServiceTests {

    String BASE_PATH = "src/main/resources/";

    @Test
    public void shouldExtendFoundedStickLineForOneSideOnly() throws DetectorException, CueServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = BASE_PATH + "jacek" ;

        Detector detector = new Detector();
        Drawer drawer = new Drawer();

        Detector detector1 = mock(Detector.class);
        Properties properties = mock(Properties.class);
        Point point = mock(Point.class);
        CueService cueService = new CueService(properties, detector1);

        for (int i = 0; i < 9; i++){
            String file = fileUrl + (i+1) + ".png";
            String fileTemp = fileUrl + (i+1) + "-temp.png";

            Mat sourceWithStickImg = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

            detector.setSourceImg(sourceWithStickImg.clone());
            Line line = detector.findStickLine();
            Line extendedLine = cueService.getExtendedStickLineForOneSide(line);


            when(detector1.isPointInsideBand(point)).thenReturn(true);

            Imgproc.line(sourceWithStickImg, line.getBegin(), line.getEnd(), new Scalar(110, 0, 255), 9);

            Mat dst = drawer.drawBandLines(sourceWithStickImg);
            Imgcodecs.imwrite(fileTemp, dst);
        }
    }


    @Test
    public void shouldExtendFoundedStickLineForOneSideOnly2() throws DetectorException, CueServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = BASE_PATH + "jacek" ;

        Detector detector = new Detector();
        Drawer drawer = new Drawer();

        Properties properties = new Properties();
        CueService cueService = new CueService(properties, detector);

        for (int i = 0; i < 9; i++){
            String file = fileUrl + (i+1) + ".png";
            String fileTemp = fileUrl + (i+1) + "-temp.png";

            Mat sourceWithStickImg = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

            detector.setSourceImg(sourceWithStickImg.clone());
            Line line = detector.findStickLine();
            Line extendedLine = cueService.getExtendedStickLineForOneSide(line);

            Imgproc.line(sourceWithStickImg, line.getBegin(), line.getEnd(), new Scalar(110, 0, 255), 5);
            Imgproc.line(sourceWithStickImg, extendedLine.getBegin(), extendedLine.getEnd(), new Scalar(0, 0, 255), 9);

            Mat dst = drawer.drawBandLines(sourceWithStickImg);
            Imgcodecs.imwrite(fileTemp, dst);
        }
    }

}
