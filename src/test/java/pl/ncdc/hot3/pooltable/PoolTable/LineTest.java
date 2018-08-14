package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Detector.class, CueService.class, Properties.class, LineService.class})
public class LineTest {
    String BASE_PATH = "src/main/resources/";

    private LineService lineService = new LineService(new Properties(), new Detector());

    //private CueService cueService = new CueService(new Properties(), new Detector(), new LineService());
    @Autowired
    private CueService cueService;

    @Test
    public void directedLine() throws CueServiceException, LineServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String sourceImagePath = BASE_PATH + "emptyTable.png";
        Mat sourceImage = Imgcodecs.imread(sourceImagePath, Imgcodecs.IMREAD_COLOR);

        Line line1 = new Line(
                new Point(1331, 957),
                new Point(1372, 897)
        );
        Line line2 = new Line(
                new Point(1320, 951),
                new Point(1579, 581)
        );

        Imgproc.circle(sourceImage, line1.getEnd(), 50, new Scalar(0, 255, 255), 3);
        Imgproc.circle(sourceImage, line2.getEnd(), 50, new Scalar(0, 255, 255), 3);


        Line asd = lineService.getDirectedLine(line1, line2);
        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 255, 222), 6, Imgproc.LINE_AA, 0);


        int predictionsCount = 4;
        Line prevLine = asd;
        for (int i = 0; i < predictionsCount; i++){
            Line prediction = cueService.predictTrajectoryAfterBump(prevLine);
            prediction = lineService.getExtendedStickLineForOneSide(prediction);
            Imgproc.line(sourceImage, prediction.getBegin(), prediction.getEnd(), new Scalar((i*20), (i*20), (i*20)), 3, Imgproc.LINE_AA, 0);
            prevLine = prediction;
        }

        Imgproc.line(sourceImage, line1.getBegin(), line1.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, line2.getBegin(), line2.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);

        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
    }
}
