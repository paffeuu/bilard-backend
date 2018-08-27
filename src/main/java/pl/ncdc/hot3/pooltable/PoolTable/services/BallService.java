package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.Settings.BandsService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Service
public class BallService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallService.class);

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

    private final int THRESH = 200;
    private final int MAX_VAL_THRESH = 255;

    private Ball whiteBall;

    private int prevBallsIndexCounter = 0;
    private List<List<Ball>> previousBalls;

    @Autowired
    public BallService(
            Properties properties,
            BandsService bandsService
    ) {
        this.properties = properties;
        this.bandsService = bandsService;

       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ranges = new MatOfFloat(0f,256f);
        channels = new MatOfInt(0);
        histSize = new MatOfInt(2);
        mask = new Mat();
        blackLowerMask = new Scalar(0, 0, 0);
        blackHigherMask = new Scalar(180, 255, 35);

        previousBalls = new ArrayList<List<Ball>>(properties.getPrevBallsCorrectorCount());

        whiteBall = null;
    }

    public Ball getWhiteBall() {
        return whiteBall;
    }

    private void setWhiteAndBlackBall(List<Ball> balls, List<Mat> ballImgList) {
        try {
            whiteBall = balls
                    .stream()
                    .max(Comparator.comparing(Ball::getWhitePixels))
                    .orElseThrow(BallsDetectorException::new);
            whiteBall.setId(properties.getWhiteBallId());
        } catch (BallsDetectorException e) {
            System.out.println("White ball not found.");
        }

        int indexOfBlackBall = getIndexOfBall(ballImgList, blackLowerMask, blackHigherMask);
        balls.get(indexOfBlackBall).setId(properties.getBlackBallId());
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

    public List<Ball> createListOfBalls(Mat circles, Mat sourceImg, List<Rect> roiList) {

        List<Ball> detectedBalls;

        // Make a list of balls from a MAT that includes x,y,r of a Ball
        detectedBalls = convertMatToListOfBalls(circles);

        // Create list of images from list of rectangles, it creates new Mat around every detected ball
        List<Mat> ballImgList = cropImage(roiList, sourceImg);

        // Set number of white pixels on every detected ball
        differentiateStripesAndSolids(sourceImg, detectedBalls, roiList);

        // Set white ball id to 0 and black ball id to 8
        setWhiteAndBlackBall(detectedBalls, ballImgList);

        int solidId = properties.getFirstSolidBallId();
        int stripedId = properties.getFirstStripedBallId();
        int rectangleSideLength = 2 * properties.getBallExpectedRadius();

        // Set id of every ball excluding white and black ball
        for(Ball ball : detectedBalls) {
            if(ball.getId() == Ball.DEFAULT_ID) {
                if ((ball.getWhitePixels() * 100) / Math.pow(rectangleSideLength, 2) >= properties.getWhitePixelsPercentageBorder()) {
                    ball.setId(stripedId);
                    stripedId++;
                } else {
                    ball.setId(solidId);
                    solidId++;
                }
            }
        }

        // Stabilize with previous detected balls
        detectedBalls = stabilizeWithPrevious(detectedBalls);

        // Sort list of balls by id
        Collections.sort(detectedBalls);

        return detectedBalls;
    }

    private List<Ball> stabilizeWithPrevious(List<Ball> currentList) {
        if (currentList != null && !currentList.isEmpty()) {
            prevBallsIndexCounter = (++prevBallsIndexCounter) % properties.getPrevBallsCorrectorCount();
            if (previousBalls.size() < properties.getPrevBallsCorrectorCount()){
                previousBalls.add(currentList);
            } else {
                previousBalls.set(prevBallsIndexCounter, currentList);
            }

            List<Ball> listOfApprovedBalls = new ArrayList<>();
            int[] ballsApprovedWithPrevious = new int[currentList.size()];

            int currentBallIndex = 0;
            for (Ball currentBall : currentList) {
                for (int i = 0; i < properties.getPrevBallsCorrectorCount() - 1; i++) {
                    int tempBallListIndex = (prevBallsIndexCounter + i) % properties.getPrevBallsCorrectorCount();

                    if (previousBalls.get(tempBallListIndex) != null &&
                            isBallInPreviousList(currentBall, previousBalls.get(tempBallListIndex))){
                        ballsApprovedWithPrevious[currentBallIndex] += 1;
                    }
                }

                if (ballsApprovedWithPrevious[currentBallIndex] >= Math.floor(previousBalls.size() * 0.8)){
                    listOfApprovedBalls.add(currentBall);
                }
                currentBallIndex++;
            }
            currentList = listOfApprovedBalls;
        }
        return currentList;
    }

    private boolean isBallInPreviousList(Ball ball, List<Ball> listOfPreviousBallsPosition) {
        double prevPositionTolerance = properties.getBallExpectedRadius() / 2;

        for (Ball currentBall : listOfPreviousBallsPosition) {
            if (LineService.getDistanceBetweenPoints(ball.getCenter(), currentBall.getCenter()) <= prevPositionTolerance){
                return true;
            }
        }

        return false;
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
        Imgproc.threshold(firstPlaneEqualized, firstPlaneThreshold, THRESH,MAX_VAL_THRESH, Imgproc.THRESH_BINARY);
        Imgproc.threshold(secondPlaneEqualized, secondPlaneThreshold, THRESH,MAX_VAL_THRESH, Imgproc.THRESH_BINARY);

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
