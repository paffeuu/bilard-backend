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
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.BandsService;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class LineTest {
    String BASE_PATH = "src/main/resources/";

    public double[] calcAllCoordinate(Line line) {
        double Y = line.getBegin().y - line.getEnd().y;
        double X = line.getBegin().x - line.getEnd().x;

        double a = Y / (X == 0 ? 0.1 : X);
        double b = line.getBegin().y - line.getBegin().x * a;
        return new double[]{a, -1, b};
    }

    @Test
    public void testForCoordinatesForVerticalLine(){


        Point begin = new Point(20, 100);
        Point end = new Point(20, 200);
        Line line = new Line(begin, end);

        double[] AB = LineService.calcCoordinatesAB(line);
        System.out.println("A : " + AB[0] + "B : " + AB[1]);

        double[] ABC = calcAllCoordinate(line);
        System.out.println("A: " + ABC[0] + "B: " + ABC[1] + "C: " + ABC[2]);

    }


//    @Test
//    public void directedLine() throws LineServiceException, DetectorException, CueServiceException {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        String sourceImagePath = BASE_PATH + "jacek7.png";
//        Mat sourceImage = Imgcodecs.imread(sourceImagePath, Imgcodecs.IMREAD_COLOR);
//
//        detector.setSourceImg(sourceImage.clone());
//
//        Line asd = null;
//        try {
//            asd = detector.findStickLine();
//
//        } catch (MissingCueLineException e) {
//            System.out.println(e.getMessage());
//            return;
//        }
//
//        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//        Imgproc.circle(sourceImage, asd.getBegin(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
//
//        Line prevLine = asd;
//        for (int i = 0; i < properties.getPredictionDepth(); i++){
//
//            Line prediction = cueService.predictTrajectoryAfterBump(prevLine);
//
//            Imgproc.line(sourceImage, prediction.getBegin(), prediction.getEnd(), new Scalar(0, 111, 255), 3, Imgproc.LINE_AA, 0);
//            Imgproc.circle(sourceImage, prediction.getBegin(), 30, new Scalar(0, 111, 255), 3);
//            Imgproc.circle(sourceImage, prediction.getEnd(), 30, new Scalar(0, 111, 255), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
//
//            prevLine = prediction;
//        }
//
//        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
//    }
//
//    @Test
//    public void calculateCordinates() {
//        Line line = new Line(
//                new Point(0, -4),
//                new Point(2, 0)
//        );
//        Point point = new Point(2, 0);
//
//        double[] cordinates = cueService.calcAllCordinate(line);
//        double distance = cueService.calculateDistanceBetwenPointAndLine(point, line);
//        System.out.print(cordinates[0] + " | " + cordinates[1] + " | " + cordinates[2] + " | " + distance);
//    }
//
//    @Test
//    public void stopLineAtFirstBall() {
//        Line line = new Line(
//                new Point(0, -4),
//                new Point(2, 0)
//        );
//        ArrayList<Ball> balls = new ArrayList<>();
//        balls.add(new Ball(0, 3, 0, 2));
//
//        Ball stoped = cueService.stopLineAtFirstBall(line, balls, false);
//        System.out.print("asd");
//    }
//
//    @Test
//    public void ballColision() throws LineServiceException, DetectorException, CueServiceException {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        String sourceImagePath = BASE_PATH + "jacek6.png";
//        Mat sourceImage = Imgcodecs.imread(sourceImagePath, Imgcodecs.IMREAD_COLOR);
//
//        detector.setSourceImg(sourceImage.clone());
//
//        Line asd = null;
//        try {
//            asd = detector.findStickLine();
//
//        } catch (MissingCueLineException e) {
//            System.out.println(e.getMessage());
//            return;
//        }
//
//        ArrayList<Ball> balls = detector.createListOfBalls();
//
//        Ball colision1 = cueService.stopLineAtFirstBall(asd, balls, true);
//
//        if (null != colision1) {
////                Imgproc.circle(sourceImage, new Point(colision.getX(), colision.getY()), 30, new Scalar(0, 111, 255), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
////                Imgproc.circle(sourceImage, c[0], 30, new Scalar(255, 0, 0), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
////                Imgproc.circle(sourceImage, c[1], 30, new Scalar(255, 0, 0), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
//            Line celownik = cueService.findBallColisionLine(asd, colision1);
//            celownik = lineService.getExtendedStickLineForOneSide(celownik);
//            Imgproc.line(sourceImage, celownik.getBegin(), celownik.getEnd(), new Scalar(0, 111, 255), 3, Imgproc.LINE_AA, 0);
//            Imgproc.circle(sourceImage, celownik.getBegin(), new Double(colision1.getRadius()).intValue(), new Scalar(0, 255, 255), 3);
//        }
//
//        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//        Imgproc.circle(sourceImage, asd.getBegin(), 50, new Scalar(255, 0, 0), 3);
//        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
//
//
//        Line prevLine = asd;
//        for (int i = 0; i < properties.getPredictionDepth(); i++){
//
//            Line prediction = cueService.predictTrajectoryAfterBump(prevLine);
//            Ball colision = cueService.stopLineAtFirstBall(prediction, balls, false);
//
//            if (null != colision) {
////                Imgproc.circle(sourceImage, new Point(colision.getX(), colision.getY()), 30, new Scalar(0, 111, 255), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
////                Imgproc.circle(sourceImage, c[0], 30, new Scalar(255, 0, 0), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
////                Imgproc.circle(sourceImage, c[1], 30, new Scalar(255, 0, 0), 3); prediction = lineService.getExtendedStickLineForOneSide(prediction);
//                Line celownik = cueService.findBallColisionLine(prediction, colision);
//                celownik = lineService.getExtendedStickLineForOneSide(celownik);
//                Imgproc.line(sourceImage, celownik.getBegin(), celownik.getEnd(), new Scalar(0, 111, 255), 3, Imgproc.LINE_AA, 0);
//                Imgproc.circle(sourceImage, celownik.getBegin(), new Double(colision.getRadius()).intValue(), new Scalar(0, 255, 255), 3);
//                prediction.setEnd(celownik.getBegin());
//            }
//
//            Imgproc.line(sourceImage, prediction.getBegin(), prediction.getEnd(), new Scalar(0, 111, 255), 3, Imgproc.LINE_AA, 0);
//
//            if (null != colision) {
//                break;
//            }
//
//            prevLine = prediction;
//        }
//
//        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
//    }
}
