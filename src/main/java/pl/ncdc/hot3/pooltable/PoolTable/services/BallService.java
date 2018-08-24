package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.Settings.BandsService;

import java.util.*;


@Service
public class BallService {

    private Properties properties;
    private BandsService bandsService;

    //small variables but used in two methods, no point to initialize them twice
    private MatOfFloat ranges;
    private MatOfInt channels;
    private MatOfInt histSize;
    private Mat mask;

    // masks to differentiate and set unique id of every ball
    private Scalar blackLowerMask;
    private Scalar blackHigherMask;

    private Ball whiteBall;

    @Autowired
    public BallService(
            Properties properties,
            BandsService bandsService
    ) {
        this.properties = properties;
        this.bandsService = bandsService;

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ranges = new MatOfFloat(0f,256f);
        channels = new MatOfInt(0);
        histSize = new MatOfInt(2);
        mask = new Mat();
        blackLowerMask = new Scalar(0, 0, 0);
        blackHigherMask = new Scalar(180, 255, 35);

        whiteBall = null;
    }

    Ball getWhiteBall() {
        return whiteBall;
    }

    private void setWhiteAndBlackBall(List<Ball> balls, List<Mat> ballImgList) {
        try {
            whiteBall = balls
                    .stream()
                    .max(Comparator.comparing(Ball::getWhitePixels))
                    .orElseThrow(BallsDetectorException::new);
            whiteBall.setId(0);
        } catch (BallsDetectorException e) {
            System.out.println("White ball not found.");
        }

        int indexOfBlackBall = getIndexOfBall(ballImgList, blackLowerMask, blackHigherMask);
        balls.get(indexOfBlackBall).setId(8);
    }

    private List<Ball> convertMatToListOfBalls(Mat circles) {
        int x;
        int y;
        int r;
        List<Ball> balls = new ArrayList<>();

        for (int i = 0; i < circles.cols(); i++) {
            // read ball coordinates
            double[] data = circles.get(0, i);

            x = (int) data[0];
            y = (int) data[1];
            r = (int) data[2];

            Ball ball = new Ball(x,y,r);
            balls.add(ball);
        }

        return balls;
    }

    private List<Mat> cropImage(List<Rect> roi, Mat image) {
        List<Mat> crops = new ArrayList<>();
        Mat crop;

        if (roi != null && !roi.isEmpty()){
            for(Rect rect : roi) {
                crop = new Mat(image, rect);
                crops.add(crop);
            }
        }

        return crops;
    }

    List<Ball> createListOfBalls(Mat circles, Mat sourceImg, List<Rect> roiList) {

        List<Ball> detectedBalls = new ArrayList<>();

        if(circles.get(0,0)[0] < properties.getTableBandLeft()
                || circles.get(0,0)[0] > properties.getTableBandRight()
                || circles.get(0,0)[1] < properties.getTableBandTop()
                || circles.get(0,0)[1] > properties.getTableBandBottom()){

                return detectedBalls;
        }

        // Make a list of balls from a MAT that includes x,y,r of a Ball
        detectedBalls = convertMatToListOfBalls(circles);

        // Create list of images from list of rectangles, it creates new Mat around every detected ball
        List<Mat> ballImgList = cropImage(roiList, sourceImg);

        // Set number of white pixels on every detected ball
        differentiateStripesAndSolids(sourceImg, detectedBalls, roiList);

        // Set white ball id to 0 and black ball id to 8
        setWhiteAndBlackBall(detectedBalls, ballImgList);

        // Set id of every ball excluding white and black ball
        int solidId = 10;
        int stripedId = 30;
        for(Ball ball : detectedBalls) {
            if(ball.getId() == null) {
                if ((ball.getWhitePixels() * 100) / 1764 >= 16) {
                    ball.setId(stripedId);
                    stripedId++;
                } else {
                    ball.setId(solidId);
                    solidId++;
                }
            }
        }

        // Sort list of balls by id
        Collections.sort(detectedBalls);

        return detectedBalls;
    }

    private int getIndexOfBall(List<Mat> ballImgList, Scalar lowerMask, Scalar higherMask) {
        Mat convertedImg = new Mat();
        Mat temporaryBallImg = new Mat();
        Mat temporaryHist = new Mat();
        double numberOfWhitePixels = 0;
        int indexOfBall = 0;

        for(int i = 0 ; i < ballImgList.size() ; i ++) {
            // convert every ball image into HSV
            Imgproc.cvtColor(ballImgList.get(i), convertedImg, Imgproc.COLOR_BGR2HSV);

            // change every pixel between mask bands into white pixels, and the others into black pixels
            Core.inRange(convertedImg, lowerMask, higherMask, temporaryBallImg);

            // calculate number of white pixels
            Imgproc.calcHist(Collections.singletonList(temporaryBallImg), channels, mask, temporaryHist, histSize ,ranges);

            // update number of white pixels and index of ball that interests us
            if(temporaryHist.get(1,0)[0] > numberOfWhitePixels) {
                indexOfBall = i;
                numberOfWhitePixels = temporaryHist.get(1,0)[0];
            }
        }

        // clear the memory
        convertedImg.release();
        temporaryBallImg.release();
        temporaryHist.release();

        return indexOfBall;
    }

    private void differentiateStripesAndSolids(Mat circles, List<Ball> detectedBalls, List<Rect> roiList) {
        // split source image into layers
        List<Mat> planes = new ArrayList<>(3);
        Core.split(circles, planes);

        Mat firstPlaneEqualized = new Mat();
        Mat secondPlaneEqualized = new Mat();
        Mat firstPlaneThreshold = new Mat();
        Mat secondPlaneThreshold = new Mat();

        // image processing on B and G layers of RGB image
        Imgproc.equalizeHist(planes.get(0), firstPlaneEqualized);
        Imgproc.equalizeHist(planes.get(1), secondPlaneEqualized);
        Imgproc.threshold(firstPlaneEqualized, firstPlaneThreshold,200,255,Imgproc.THRESH_BINARY);
        Imgproc.threshold(secondPlaneEqualized, secondPlaneThreshold,200,255,Imgproc.THRESH_BINARY);

        // releasing memory from unused Mat objects
        planes.get(0).release();
        planes.get(1).release();
        firstPlaneEqualized.release();
        secondPlaneEqualized.release();

        // creating list of Mat objects, where every single ball image is different mat on B and G layer
        List<Mat> listOfB = cropImage(roiList, firstPlaneThreshold);
        List<Mat> listOfG = cropImage(roiList, secondPlaneThreshold);

        // releasing memory from unused Mat objects
        firstPlaneThreshold.release();
        secondPlaneThreshold.release();

        Mat histB = new Mat();
        Mat histG = new Mat();

        /* loop that checks every image on the list, calculates the number of white pixels on it and sets
           whitePixels field on Ball class */
        for(int k = 0 ; k < listOfB.size() ; k ++) {
            Imgproc.calcHist(Collections.singletonList(listOfB.get(k)), channels, mask, histB, histSize ,ranges);
            Imgproc.calcHist(Collections.singletonList(listOfG.get(k)), channels, mask, histG, histSize ,ranges);

            if(histB.get(1,0)[0] > 3 * histG.get(1,0)[0]) {
                detectedBalls.get(k).setWhitePixels(histG.get(1,0)[0]);
            }else if(histG.get(1,0)[0] > 3 * histB.get(1,0)[0]) {
                detectedBalls.get(k).setWhitePixels(histB.get(1,0)[0]);
            }else {
                detectedBalls.get(k).setWhitePixels((histB.get(1, 0)[0] + histG.get(1, 0)[0])/2);
            }
        }

        // clear the memory
        listOfB.clear();
        listOfG.clear();
        histB.release();
        histG.release();
    }
}
