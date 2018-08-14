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
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
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

    @Autowired
    private CueService cueService;

    @Autowired
    private Detector detector;

    @Autowired
    private LineService lineService;

    @Autowired
    private Properties properties;

    @Test
    public void directedLine() throws LineServiceException, DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String sourceImagePath = BASE_PATH + "jacek5.png";
        Mat sourceImage = Imgcodecs.imread(sourceImagePath, Imgcodecs.IMREAD_COLOR);

        detector.setSourceImg(sourceImage.clone());

        Line asd = null;
        try {
            asd = detector.findStickLine();

        } catch (MissingCueLineException e) {
            System.out.println(e.getMessage());
            return;
        }

        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.circle(sourceImage, asd.getBegin(), 50, new Scalar(0, 255, 255), 3);
        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);

        Line prevLine = asd;
        for (int i = 0; i < properties.getPredictionDepth(); i++){

            Line prediction = cueService.predictTrajectoryAfterBump(prevLine);
            prediction = lineService.getExtendedStickLineForOneSide(prediction);

            Imgproc.line(sourceImage, prediction.getBegin(), prediction.getEnd(), new Scalar(0, 111, 255), 3, Imgproc.LINE_AA, 0);
            Imgproc.circle(sourceImage, prediction.getBegin(), 30, new Scalar(0, 111, 255), 3);
            Imgproc.circle(sourceImage, prediction.getEnd(), 30, new Scalar(0, 111, 255), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);

            prevLine = prediction;
        }

        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
    }
}
