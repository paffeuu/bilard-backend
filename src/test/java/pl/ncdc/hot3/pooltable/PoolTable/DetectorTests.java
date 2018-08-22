package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.ExtendLineException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.MissingCueLineException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.BallService;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Settings.BandsService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Settings.PathService;
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
        ImageUndistorterService.class,
        PathService.class,
        BandsService.class
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

    @Autowired
    LineService lineService;

    @Autowired
    BallService ballService;

    @Autowired
    PathService pathService;

    @Autowired
    BandsService bandsService;

    @Test
    public void shouldFindStickLineAndSaveImageWithLine() throws DetectorException, MissingCueLineException, FileNotFoundException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat source = Imgcodecs.imread(pathService.TESTS_PATH + "jacek1.jpg", CvType.CV_64F);
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
    public void tresholdingTests() throws FileNotFoundException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat emptyTable = Imgcodecs.imread(pathService.getFullPath("emptyTable.png"), Imgcodecs.IMREAD_COLOR);
        Imgproc.cvtColor(emptyTable, emptyTable, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(emptyTable, emptyTable, 120, 255, Imgproc.THRESH_BINARY);

        Imgcodecs.imwrite("tests/edges/empty1.png", emptyTable);

        for (int imgCounter = 1; imgCounter <= 500; imgCounter++) {
            Mat sClone1 = new Mat();

            String filename = pathService.TESTS_PATH + "before/test_before" + imgCounter + ".jpg";
            Mat source = Imgcodecs.imread( filename, CvType.CV_64F);
            Mat sCloneOrigin = source.clone();


            Imgproc.blur(source, source, new Size(3,3));
            Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(source, source, 140, 255, Imgproc.THRESH_BINARY);

            Imgproc.Canny(source, sClone1, 100, 40, 3, false);
            Imgcodecs.imwrite("tests/edges/canny/canny" + imgCounter + ".png", sClone1);

            Mat forSub = new Mat();

            Core.subtract(sClone1, emptyTable, forSub);

            Mat linesData1 = new Mat();
            Imgproc.HoughLinesP(forSub, linesData1, 1, Math.PI/180, 70, 50, 10);

            int approvedLinesCounter = 0;
            List <Line> linesList = new ArrayList<>();
            for (int i = 0; i < linesData1.rows(); i++){
                double line[] = linesData1.get(i, 0);

                Line tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
                if (bandsService.isPointInsideBand(tempLine.getBegin()) || bandsService.isPointInsideBand(tempLine.getEnd())){
                    linesList.add(tempLine);
                    approvedLinesCounter++;
                }
            }

            int indexOfLine_A = 0, indexOfLine_B = 0;
            double a1, a2, pMin = 5;


            for (int i = 0; i < linesList.size() - 1; i++){
                for (int j = 0; j < linesList.size(); j++){
                    if (i != j) {

                        a1 = cueService.calcAllCordinate(linesList.get(i))[0];
                        a2 = cueService.calcAllCordinate(linesList.get(j))[0];

                        if (Math.abs(a1 - a2) < pMin) {
                            pMin = Math.abs(a1 - a2);
                            indexOfLine_A = i;
                            indexOfLine_B = j;
                        }
                    }
                }
            }

            Line newLineBetweenShort = null;
            Line newLineBetweenLong = null;
            if (indexOfLine_A != indexOfLine_B) {
                double X1 = (linesList.get(indexOfLine_A).getBegin().x + linesList.get(indexOfLine_B).getBegin().x) / 2;
                double X2 = (linesList.get(indexOfLine_A).getEnd().x + linesList.get(indexOfLine_B).getEnd().x) / 2;
                double Y1 = (linesList.get(indexOfLine_A).getBegin().y + linesList.get(indexOfLine_B).getBegin().y) / 2;
                double Y2 = (linesList.get(indexOfLine_A).getEnd().y + linesList.get(indexOfLine_B).getEnd().y) / 2;
                Point newBegin = new Point(X1, Y1);
                Point newEnd = new Point(X2, Y2);
                newLineBetweenShort = new Line(newBegin, newEnd);
            }

            if (newLineBetweenShort != null) {
                try {
                    newLineBetweenLong = lineService.getExtendedStickLineForBothSides(newLineBetweenShort);
                } catch (ExtendLineException e) {
                    System.out.println("Cannot extend it: " + newLineBetweenShort);
                }
            }

            if (newLineBetweenLong != null && newLineBetweenShort != null) {
                double b2b = CueService.getDistanceBetweenPoints(newLineBetweenShort.getBegin(), newLineBetweenLong.getBegin());
                double b2e = CueService.getDistanceBetweenPoints(newLineBetweenShort.getBegin(), newLineBetweenLong.getEnd());
                double e2b = CueService.getDistanceBetweenPoints(newLineBetweenShort.getEnd(), newLineBetweenLong.getBegin());
                double e2e = CueService.getDistanceBetweenPoints(newLineBetweenShort.getEnd(), newLineBetweenLong.getEnd());

            }

            try {
                Line para1 = linesList.get(indexOfLine_A);
                Line para2 = linesList.get(indexOfLine_B);

                if (newLineBetweenLong != null) {
                    Imgproc.line(sCloneOrigin, newLineBetweenLong.getBegin(), newLineBetweenLong.getEnd(), new Scalar(255, 0, 0), 5);
                }
                if (newLineBetweenShort != null) {
                    Imgproc.line(sCloneOrigin, newLineBetweenShort.getBegin(), newLineBetweenShort.getEnd(), new Scalar(0, 255, 0), 6);
                }
                Imgproc.line(sCloneOrigin, para1.getBegin(), para1.getEnd(), new Scalar(0, 0, 255), 4);
                Imgproc.line(sCloneOrigin, para2.getBegin(), para2.getEnd(), new Scalar(0, 0, 255), 4);
            } catch (Exception e){
                System.out.println("Exception msg: " + e.getMessage());
            }


            Imgcodecs.imwrite("tests/edges/out" + imgCounter + ".png", sCloneOrigin);

            forSub.release();
            source.release();
            sClone1.release();
            sCloneOrigin.release();
            linesData1.release();
        }
        emptyTable.release();
    }

    @Test
    public void shouldFindStickEvenForHardCouses(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List <Mat> layers = new ArrayList<>();

        Mat source = Imgcodecs.imread(pathService.TESTS_PATH + "jacek1.jpg", CvType.CV_64F);
        source = undistorterService.undistort(source);

        Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
        Core.split(source, layers);

        Imgproc.Canny(layers.get(2), source, 50, 200, 3, false);
        Imgproc.threshold(source, source, 200, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("tests/treshed.png", source);

        Core.subtract(source, detector.getEmptyTableImage(), source);


        Imgcodecs.imwrite("tests/substracted.png", source);
    }

    @Test
    public void shouldFindBallsEvenForHardCouses() throws BallsDetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List <Mat> layers = new ArrayList<>();

        Mat source = Imgcodecs.imread(pathService.TESTS_PATH + "emptyTable.png", CvType.CV_64F);

        detector.setSourceImg(source);
        List <Ball> listOfBalls = detector.createListOfBalls();

        System.out.println(listOfBalls.size());
        Imgcodecs.imwrite("tests/substracted.png", source);
    }


}
