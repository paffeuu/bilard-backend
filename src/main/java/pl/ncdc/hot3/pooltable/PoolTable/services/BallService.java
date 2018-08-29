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
    private Scalar blackLowerMask = new Scalar(120, 2, 190);
    private Scalar blackHigherMask = new Scalar(152, 30, 240);
    private Scalar purpleLowerMask = new Scalar(142, 49, 162);
    private Scalar purpleHigherMask = new Scalar(174, 118, 235);
    private Scalar orangeLowerMask = new Scalar(3,120,239);
    private Scalar orangeHigherMask = new Scalar(11,161,240);
    private Scalar blueLowerMask = new Scalar(143, 66, 239);
    private Scalar blueHigherMask = new Scalar(160, 120, 240);
    private Scalar yellowLowerMask = new Scalar(32, 120, 239);
    private Scalar yellowHigherMask = new Scalar(40, 169, 240);
    private Scalar red1LowerMask = new Scalar(0, 117, 239);
    private Scalar red1HigherMask = new Scalar(5, 120, 240);
    private Scalar red2LowerMask = new Scalar(230, 96, 239);
    private Scalar red2HigherMask = new Scalar(240, 142, 240);
    private Scalar brown1LowerMask = new Scalar(0, 40, 239);
    private Scalar brown1HigherMask = new Scalar(5, 95, 240);
    private Scalar brown2LowerMask = new Scalar(230, 40, 239);
    private Scalar brown2HigherMask = new Scalar(240, 95, 240);
    private Scalar greenLowerMask = new Scalar(102,29,239);
    private Scalar greenHigherMask = new Scalar(136,53,240);

    private final int THRESH = 200;
    private final int MAX_VAL_THRESH = 255;

    private Ball whiteBall;

    private int prevBallsIndexCounter;
    private List<List<Ball>> previousBalls;
    private List<Ball> staticBalls;

    @Autowired
    public BallService(
            Properties properties,
            BandsService bandsService
    ) {
        this.properties = properties;
        this.bandsService = bandsService;

        ranges = new MatOfFloat(0f,256f);
        channels = new MatOfInt(0);
        histSize = new MatOfInt(2);
        mask = new Mat();

        previousBalls = new ArrayList<List<Ball>>(properties.getPreviousBallsPositionsToCompare());
        prevBallsIndexCounter = 0;
        staticBalls = new ArrayList<>();

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

        List<Ball> solidBalls = new ArrayList<>();
        List<Ball> stripedBalls = new ArrayList<>();
        List<Mat> solidBallsImg = new ArrayList<>();
        List<Mat> stripedBallsImg = new ArrayList<>();

        List<Ball> finalBallList = new ArrayList<>();

        // Set id of every ball excluding white and black ball
        for(int i = 0 ; i < detectedBalls.size() ; i ++) {
            if(detectedBalls.get(i).getId() == Ball.DEFAULT_ID) {
                if ((detectedBalls.get(i).getWhitePixels() * 100) / Math.pow(rectangleSideLength, 2) >= properties.getWhitePixelsPercentageBorder()) {
                    stripedBalls.add(detectedBalls.get(i));
                    stripedBallsImg.add(ballImgList.get(i));
                } else {
                    solidBalls.add(detectedBalls.get(i));
                    solidBallsImg.add(ballImgList.get(i));
                }
            } else {
                finalBallList.add(detectedBalls.get(i));
            }
        }

        detectedBalls.clear();

        setIndexOfBall(solidBalls, solidBallsImg, yellowLowerMask, yellowHigherMask, 10);
        setIndexOfBall(solidBalls, solidBallsImg, blueLowerMask, blueHigherMask, 11);

        setIndexOfBall(solidBalls, solidBallsImg, purpleLowerMask, purpleHigherMask, 13);
        setIndexOfBall(solidBalls, solidBallsImg, orangeLowerMask, orangeHigherMask, 14);
        setIndexOfBall(solidBalls, solidBallsImg, greenLowerMask, greenHigherMask, 15);

        setIndexOfBall(solidBalls, solidBallsImg, yellowLowerMask, yellowHigherMask, 30);
        setIndexOfBall(solidBalls, solidBallsImg, blueLowerMask, blueHigherMask, 31);

        setIndexOfBall(solidBalls, solidBallsImg, purpleLowerMask, purpleHigherMask, 33);
        setIndexOfBall(solidBalls, solidBallsImg, orangeLowerMask, orangeHigherMask, 34);
        setIndexOfBall(solidBalls, solidBallsImg, greenLowerMask, greenHigherMask, 35);

        finalBallList.addAll(solidBalls);
        finalBallList.addAll(stripedBalls);

        return finalBallList;
    }


    private void setIndexOfBall(List<Ball> balls, List<Mat> ballImgList, Scalar lowerMask, Scalar higherMask, int id) {
        int index = getIndexOfBall(balls, ballImgList, lowerMask, higherMask);
        balls.get(index).setId(id);
    }

    private int getIndexOfBall(List<Ball> balls, List<Mat> ballImgList, Scalar lowerMask, Scalar higherMask) {
        double numberOfWhitePixels = 0;
        int indexOfBall = 0;
        Mat temporaryMat = new Mat();
        Mat inRangeMat = new Mat();
        Mat histMat = new Mat();

        for(int i = 0 ; i < ballImgList.size() ; i ++) {
            Imgproc.cvtColor(ballImgList.get(i), temporaryMat, Imgproc.COLOR_BGR2HLS);
            Core.inRange(temporaryMat, lowerMask, higherMask, inRangeMat);
            Imgproc.calcHist(Collections.singletonList(inRangeMat), channels, mask, histMat, histSize, ranges);

            if(histMat.get(1,0)[0] > numberOfWhitePixels) {
                numberOfWhitePixels = histMat.get(1,0)[0];
                indexOfBall = i;
            }
        }

        return indexOfBall;
    }

    private List<Ball> stabilizeWithPrevious(List<Ball> currentList) {
        if (currentList != null && !currentList.isEmpty()) {
            if (previousBalls.size() < properties.getPreviousBallsPositionsToCompare()){
                previousBalls.add(currentList);
            } else {
                previousBalls.set(prevBallsIndexCounter, currentList);
            }
            prevBallsIndexCounter = (++prevBallsIndexCounter) % properties.getPreviousBallsPositionsToCompare();

            List<Ball> listOfApprovedBalls = new ArrayList<>();
            int[] ballsApprovePoints = new int[currentList.size()];

            int currentBallIndex = 0;
            for (Ball currentBall : currentList) {
                for (int i = 0; i < properties.getPreviousBallsPositionsToCompare() - 1; i++) {
                    int tempBallListIndex = (prevBallsIndexCounter + i) % properties.getPreviousBallsPositionsToCompare();

                    if (previousBalls.get(tempBallListIndex) != null &&
                            indexOfBallInPreviousList(currentBall, previousBalls.get(tempBallListIndex)) != -1){
                        ballsApprovePoints[currentBallIndex] += 1;
                    }
                }

                if (ballsApprovePoints[currentBallIndex] >= Math.floor(previousBalls.size() * 0.8)){
                    listOfApprovedBalls.add(currentBall);
                }
                currentBallIndex++;
            }
            if (!listOfApprovedBalls.isEmpty()){
                currentList = listOfApprovedBalls;
            }
        }

        return currentList;
    }

    public List<Ball> stabilize(List<Ball> currentBallList){
        // Stabilize with previous detected balls
        currentBallList = stabilizeWithPrevious(currentBallList);

        if (currentBallList == null || currentBallList.isEmpty()){
            return staticBalls;
        }

        if (staticBalls.isEmpty()) {
            staticBalls = currentBallList;
            return staticBalls;
        }

        for (Ball currentBall : currentBallList) {
            int idx = indexOfBallInPreviousList(currentBall, staticBalls);
            if (idx >= 0){
                currentBall.setCenter(staticBalls.get(idx).getCenter());
            } else {
                staticBalls.add(currentBall);
            }
        }

        if (staticBalls.size() > currentBallList.size()) {
            for (Ball currentBall : staticBalls){
                if (indexOfBallInPreviousList(currentBall, currentBallList) == -1){
                    staticBalls.remove(currentBall);
                }
            }
        }

        return staticBalls;
    }

    private int indexOfBallInPreviousList(Ball ball, List<Ball> listOfPreviousBallsPosition) {
        double prevPositionTolerance = properties.getBallExpectedRadius() / 2;

        int index = -1;
        for (Ball currentBall : listOfPreviousBallsPosition) {
            index++;
            if (LineService.getDistanceBetweenPoints(ball.getCenter(), currentBall.getCenter()) <= prevPositionTolerance){
                return index;
            }
        }

        return -1;
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
