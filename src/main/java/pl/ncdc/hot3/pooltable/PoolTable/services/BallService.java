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

import java.util.*;


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
    private Scalar whiteLowerMask = new Scalar(254,254,254);
    private Scalar whiteHigherMask = new Scalar(255,255,255);

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
        blackLowerMask = new Scalar(0, 0, 0);
        blackHigherMask = new Scalar(50, 21, 17);

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

        setIndexOfBall(balls, ballImgList, blackLowerMask, blackHigherMask, properties.getBlackBallId());
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

        int rectangleSideLength = 2 * (2 + properties.getBallExpectedRadius());

        int solidId = properties.getFirstSolidBallId();
        int stripedId = properties.getFirstStripedBallId();

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

        return detectedBalls;
    }


    public void setIndexOfBall(List<Ball> balls, List<Mat> ballImgList, Scalar lowerMask, Scalar higherMask, int id) {
        int index = getIndexOfBall(balls, ballImgList, lowerMask, higherMask);
        if(index != 355) {
            balls.get(index).setId(id);
        }
    }

    public int getIndexOfBall(List<Ball> balls, List<Mat> ballImgList, Scalar lowerMask, Scalar higherMask) {
        double numberOfWhitePixels = 0;
        int indexOfBall = 355;
        Mat inRangeMat = new Mat();
        Mat histMat = new Mat();

        for (int i = 0; i < ballImgList.size(); i++) {
            if(balls.get(i).getId() == Ball.DEFAULT_ID) {
                Core.inRange(ballImgList.get(i), lowerMask, higherMask, inRangeMat);
                Imgproc.calcHist(Collections.singletonList(inRangeMat), channels, mask, histMat, histSize, ranges);

                if (histMat.get(1, 0)[0] > numberOfWhitePixels) {
                    numberOfWhitePixels = histMat.get(1, 0)[0];
                    indexOfBall = i; }
            }
        }

        return indexOfBall;
    }

    public List<Ball> stabilizeWithPrevious(List<Ball> currentList) {
        if (currentList != null && !currentList.isEmpty()) {
            if (previousBalls.size() < properties.getPreviousBallsPositionsToCompare()){
                previousBalls.add(currentList);
            } else {
                previousBalls.add(currentList);
                previousBalls.remove(0);
            }
            prevBallsIndexCounter = (++prevBallsIndexCounter) % properties.getPreviousBallsPositionsToCompare();

            List<Ball> listOfApprovedBalls = new ArrayList<>();
            int[] ballsApprovePoints = new int[currentList.size()];

            int currentBallIndex = 0;
            for (Ball currentBall : currentList) {
                for (int i = 0; i < properties.getPreviousBallsPositionsToCompare() - 1; i++) {
                    int tempBallListIndex = (prevBallsIndexCounter + i) % properties.getPreviousBallsPositionsToCompare();

                    if (previousBalls.size() > tempBallListIndex && previousBalls.get(tempBallListIndex) != null &&
                            indexOfBallInPreviousList(currentBall, previousBalls.get(tempBallListIndex)) != -1){
                        ballsApprovePoints[currentBallIndex] += 1;
                    }
                }

                if (ballsApprovePoints[currentBallIndex] >= Math.floor(previousBalls.size() / 3)){
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
                Point newCenter = staticBalls.get(idx).getCenter();
                if (staticBalls.get(idx).getStaticCounter() < properties.getPrevBallsCorrectorCount()) {
                    newCenter = new Point((staticBalls.get(idx).getCenter().x + currentBall.getCenter().x) / 2, (staticBalls.get(idx).getCenter().y + currentBall.getCenter().y) / 2);
                    staticBalls.get(idx).setCenter(newCenter);
                }
                staticBalls.get(idx).setId(currentBall.getId());
                currentBall.setCenter(newCenter);
                staticBalls.get(idx).increaseStaticCounter();
            } else {
                staticBalls.add(currentBall);
            }
        }

        if (staticBalls.size() > currentBallList.size()) {
            for (Iterator<Ball> iterator = staticBalls.iterator(); iterator.hasNext(); ) {
                Ball ball = iterator.next();
                if (indexOfBallInPreviousList(ball, currentBallList) == -1) {
                    iterator.remove();
                }
            }
        }

        return staticBalls;
    }

    private int indexOfBallInPreviousList(Ball ball, List<Ball> listOfPreviousBallsPosition) {
        double prevPositionTolerance = properties.getBallExpectedRadius();

        int index = -1;
        for (Ball currentBall : listOfPreviousBallsPosition) {
            index++;
            if (LineService.getDistanceBetweenPoints(ball.getCenter(), currentBall.getCenter()) <= prevPositionTolerance){
                return index;
            }
        }

        return -1;
    }

    public void differentiateStripesAndSolids(Mat circles, List<Ball> detectedBalls, List<Rect> roiList) {
        // split source image into layers
        List<Mat> planes = new ArrayList<>(3);
        Core.split(circles, planes);

        Mat firstPlaneEqualized = new Mat();
        Mat secondPlaneEqualized = new Mat();
        Mat firstPlaneThreshold = new Mat();
        Mat secondPlaneThreshold = new Mat();
        Mat thirdPlaneThreshold = new Mat();
        Mat thirdPlaneEqualized = new Mat();

        // image processing on B and G layers of RGB image
        Imgproc.equalizeHist(planes.get(0), firstPlaneEqualized);
        Imgproc.equalizeHist(planes.get(1), secondPlaneEqualized);
        Imgproc.equalizeHist(planes.get(2), thirdPlaneEqualized);
        Imgproc.threshold(firstPlaneEqualized, firstPlaneThreshold, THRESH,MAX_VAL_THRESH, Imgproc.THRESH_BINARY);
        Imgproc.threshold(secondPlaneEqualized, secondPlaneThreshold, THRESH,MAX_VAL_THRESH, Imgproc.THRESH_BINARY);
        Imgproc.threshold(thirdPlaneEqualized, thirdPlaneThreshold, THRESH,MAX_VAL_THRESH, Imgproc.THRESH_BINARY);

        // releasing memory from unused Mat objects
        planes.get(0).release();
        planes.get(1).release();
        planes.get(2).release();
        firstPlaneEqualized.release();
        secondPlaneEqualized.release();
        thirdPlaneEqualized.release();

        // creating list of Mat objects, where every single ball image is different mat on B and G layer
        List<Mat> listOfB = cropImage(roiList, firstPlaneThreshold);
        List<Mat> listOfG = cropImage(roiList, secondPlaneThreshold);
        List<Mat> listOfR = cropImage(roiList, thirdPlaneThreshold);

        // releasing memory from unused Mat objects
        firstPlaneThreshold.release();
        secondPlaneThreshold.release();
        thirdPlaneThreshold.release();

        Mat histB = new Mat();
        Mat histG = new Mat();
        Mat histR = new Mat();

        /* loop that checks every image on the list, calculates the number of white pixels on it and sets
           whitePixels field on Ball class */
        for(int k = 0 ; k < listOfB.size() ; k ++) {
            Imgproc.calcHist(Collections.singletonList(listOfB.get(k)), channels, mask, histB, histSize ,ranges);
            Imgproc.calcHist(Collections.singletonList(listOfG.get(k)), channels, mask, histG, histSize ,ranges);
            Imgproc.calcHist(Collections.singletonList(listOfR.get(k)), channels, mask, histR, histSize ,ranges);

            detectedBalls.get(k).setB(histB.get(1,0)[0]);
            detectedBalls.get(k).setG(histG.get(1,0)[0]);
            detectedBalls.get(k).setR(histR.get(1,0)[0]);


            detectedBalls.get(k).setWhitePixels((histB.get(1,0)[0] + histG.get(1,0)[0]) / 2);

        }

        // clear the memory
        listOfB.clear();
        listOfG.clear();
        histB.release();
        histG.release();
    }
}
