package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.MissingCueLineException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.BallService;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        Detector.class,
        CueService.class,
        BallService.class,
        Properties.class,
        LineService.class,
        ImageUndistorterService.class
})
public class DetectorTests {

    @Autowired
    Detector detector;

    @Autowired
    CueService cueService;

    @Autowired
    Properties properties;

    @Autowired
    ImageUndistorterService undistorterService;

    @Test
    public void shouldFindStickLineAndSaveImageWithLine() throws DetectorException, MissingCueLineException, FileNotFoundException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat source = Imgcodecs.imread(properties.TESTS_PATH + "jacek1.jpg", CvType.CV_64F);
        source = undistorterService.undistort(source);

        Mat substractedImg = detector.getEdges(source.clone());
        Imgcodecs.imwrite("tests/substractedImg.png", substractedImg);

        Mat innerLinesImage = source.clone();
        List<Line> linesList = detector.getInnerLines(substractedImg);
        if (linesList.size() > 0){
            for (Line line : linesList){
                Imgproc.line(innerLinesImage, line.getBegin(), line.getEnd(), new Scalar(0, 0, 255), 10);
            }
        }
        Imgcodecs.imwrite("tests/sourceWithAllLines.png", innerLinesImage);

        Line cueLine = cueService.findStickLine(linesList);
        Mat cueLineBeforeStabilize = source.clone();
        if (cueLine != null) {
            Imgproc.line(cueLineBeforeStabilize, cueLine.getBegin(), cueLine.getEnd(), new Scalar(255, 0, 0), 8);
        }
        Imgcodecs.imwrite("tests/sourceWithCueLine.png", cueLineBeforeStabilize);

        //cueLine = cueService.stabilize(cueLine);
        Mat cueLineStabilize = source.clone();
        if (cueLine != null) {
            Imgproc.line(cueLineStabilize, cueLine.getBegin(), cueLine.getEnd(), new Scalar(0, 255, 0), 6);
        }
        Imgcodecs.imwrite("tests/sourceWithFinalCue.png", cueLineStabilize);


        Assert.assertNotNull(cueLine);

    }

    @Test
    public void tresholdingTests(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat source = Imgcodecs.imread(properties.TESTS_PATH + "jacek1.jpg", CvType.CV_64F);

        List <Mat> layers = new ArrayList<>();

        Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
        Core.split(source, layers);
        Imgproc.Canny(layers.get(2), source, 50, 200, 3, false);


        Mat treshold = new Mat();
        Imgproc.threshold(source, treshold, 150, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("tests/treshold_1.png", treshold);

        Imgproc.threshold(source, treshold, 170, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("tests/treshold_2.png", treshold);

        Imgproc.threshold(source, treshold, 200, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("tests/treshold_3.png", treshold);


    }


}
