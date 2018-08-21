package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.*;

@Service
public class BallService {

    private Properties properties;

    //Image processing fields
    private Size blurSize = new Size(5, 5);
    private ArrayList<Ball> balls = new ArrayList<>();
    private Mat temporaryBallImg = new Mat();

    //small variables but used in two methods, no point to initialize them twice
    private MatOfFloat ranges = new MatOfFloat(0f,256f);
    private MatOfInt channels = new MatOfInt(0);
    private MatOfInt histSize = new MatOfInt(2);
    private Mat mask = new Mat();
    private Mat temporaryHist = new Mat();

    //masks needed to detect colors on balls and set id
    private Scalar blackLowerMask = new Scalar(0, 0, 0);
    private Scalar blackHigherMask = new Scalar(180, 255, 35);
    private Scalar purpleLowerMask = new Scalar(115, 100, 50);
    private Scalar purpleHigherMask = new Scalar(140, 255, 150);
    private Scalar brownLowerMask = new Scalar(170, 0, 0);
    private Scalar brownHigherMask = new Scalar(180, 255, 130);
    private Scalar orangeLowerMask = new Scalar(2,50,100);
    private Scalar orangeHigherMask = new Scalar(15,255,255);
    private Scalar blueLowerMask = new Scalar(100, 120, 100);
    private Scalar blueHigherMask = new Scalar(115, 255, 180);
    private Scalar yellowLowerMask = new Scalar(20, 50, 50);
    private Scalar yellowHigherMask = new Scalar(40, 255, 255);
    private Scalar greenLowerMask = new Scalar(80,150,0);
    private Scalar greenHigherMask = new Scalar(100,250,85);
    private Scalar red1LowerMask = new Scalar(0, 180, 80);
    private Scalar red1HigherMask = new Scalar(3, 255, 200);
    private Scalar red2LowerMask = new Scalar(150, 180, 130);
    private Scalar red2HigherMask = new Scalar(180, 255, 250);

    private Mat sourceImg;

    public Mat getSourceImg() {
        return sourceImg;
    }

    public void setSourceImg(Mat sourceImg) {
        this.sourceImg = sourceImg;
    }

    @Autowired
    public BallService(
            Properties properties
    ) {
        this.properties = properties;
    }



    public void setWhiteAndBlackBall(List<Ball> balls, List<Mat> ballImgList) {
        try {
            Ball whiteBall = balls
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

    private Mat filterCircles(Mat allCircles) throws BallsDetectorException {

        Mat filteredCircles = new Mat(1, 1, CvType.CV_64FC3); // output Mat
        Mat newMat = new Mat(1, 1, CvType.CV_64FC3); // merged new column
        List<Mat> matList = new ArrayList<>(); // 2-element list for merging in Core.hconcat
        matList.add(null);
        matList.add(null);

        // conversion to use type double data
        Mat convertedAllCircles = new Mat();
        allCircles.convertTo(convertedAllCircles, CvType.CV_64FC3);

        // write circles coordinates into an array
        double[] data = convertMatToArray(convertedAllCircles);

        if (null == data) {
            throw new BallsDetectorException("Error while trying filter circles");
        }

        // filter circles
        int j = 0;
        double x, y, r;
        for (int i = 0; i < data.length; i += 3) {

            // read coordinates
            x = data[i];
            y = data[i + 1];
            r = data[i + 2];

            // check if they are within table boundaries
            if (properties.isPointInsideBand(new Point(x, y))) {
                if (j == 0) {
                    filteredCircles.put(0, j, x, y, r);
                    matList.set(0, filteredCircles);
                } else {
                    // merge horizontally filteredCircles with newMat and save to filteredCircles
                    newMat.put(0, 0, x, y, r);
                    matList.set(1, newMat);
                    Core.hconcat(matList, filteredCircles);
                    matList.set(0, filteredCircles);
                }
                j++;
            }
        }

        return filteredCircles;
    }

    private ArrayList<Ball> convertMatToListOfBalls(Mat circles) {
        int x;
        int y;
        int r;
        balls.clear();

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

    double[] convertMatToArray(Mat mat) {
        double[] data = null;
        try {
            int size = (int) mat.total() * mat.channels();
            data = new double[size];
            mat.get(0, 0, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    List<Mat> cropImage(List<Rect> roi, Mat image) {
        List<Mat> crops = new ArrayList<>();
        Mat crop;

        for(int i = 0 ; i < roi.size() ; i ++) {
            crop = new Mat(image, roi.get(i));
            crops.add(crop);
        }

        return crops;
    }

    List<Rect> getBallsROI(double[] circles) {
        double x;
        double y;
        double r = 21;
        Point topLeft;
        Point bottomRight;
        List<Rect> roiList = new ArrayList<>();

        for (int i = 0; i < circles.length; i += 3) {
            x = circles[i];
            y = circles[i + 1];

            topLeft = new Point(x - r, y - r);
            bottomRight = new Point(x + r, y + r);

            roiList.add(new Rect(topLeft, bottomRight));
        }

        return roiList;
    }

    Mat detectBalls(Mat image) throws BallsDetectorException {
        Mat blurredImage = new Mat();
        Mat convertedTypeImage = new Mat();
        Mat destinationImage = new Mat();

        // blur convertedImage
        Imgproc.blur(image, blurredImage, blurSize);

        // convert to hsv
        Imgproc.cvtColor(blurredImage, convertedTypeImage, Imgproc.COLOR_BGR2HSV);
        blurredImage.release();

        // split into planes
        List<Mat> planes = new ArrayList<>(3);
        Core.split(convertedTypeImage, planes);
        convertedTypeImage.release();

        // detect circles
        Imgproc.HoughCircles(planes.get(2), destinationImage, Imgproc.CV_HOUGH_GRADIENT, 1.0, properties.getBallMinDistance(),
                30, 15, properties.getBallMinRadius(), properties.getBallMaxRadius());
        planes.clear();

        return filterCircles(destinationImage);
    }

    List<Ball> createListOfBalls() throws BallsDetectorException {
        Mat circles = detectBalls(sourceImg);

        List<Ball> detectedBalls = convertMatToListOfBalls(circles);
        List<Rect> roiList = getBallsROI(convertMatToArray(circles));
        circles.release();

        List<Mat> ballImgList = cropImage(roiList, sourceImg);

        differentiateStripesAndSolids(detectedBalls, roiList);
        setWhiteAndBlackBall(detectedBalls, ballImgList);

        List<Mat> stripedBallsImg = new ArrayList<>();
        List<Mat> solidBallsImg = new ArrayList<>();
        List<Ball> stripedBalls = new ArrayList<>();
        List<Ball> solidBalls = new ArrayList<>();

        for(int i = 0 ; i < detectedBalls.size() ; i ++) {
            if(detectedBalls.get(i).getId() == null) {
                if ((balls.get(i).getWhitePixels() * 100) / 1764 >= 16) {
                    stripedBallsImg.add(ballImgList.get(i));
                    stripedBalls.add(detectedBalls.get(i));
                    detectedBalls.remove(i);
                } else {
                    solidBallsImg.add(ballImgList.get(i));
                    solidBalls.add(detectedBalls.get(i));
                    detectedBalls.remove(i);
                }
            }
        }

        setOtherBalls(solidBalls, solidBallsImg, yellowLowerMask, yellowHigherMask, 1, true);
        setOtherBalls(solidBalls, solidBallsImg, blueLowerMask, blueHigherMask, 2, true);
        setRedBall(solidBalls, solidBallsImg, red1LowerMask, red1HigherMask, red2LowerMask, red2HigherMask, 3, true);
        setOtherBalls(solidBalls, solidBallsImg, purpleLowerMask, purpleHigherMask, 4, true);
        setOtherBalls(solidBalls, solidBallsImg, orangeLowerMask, orangeHigherMask, 5, true);
        setOtherBalls(solidBalls, solidBallsImg, greenLowerMask, greenHigherMask, 6, true);
        setOtherBalls(solidBalls, solidBallsImg, brownLowerMask, brownHigherMask, 7, true);

        detectedBalls.addAll(solidBalls);


        setOtherBalls(stripedBalls, stripedBallsImg, yellowLowerMask, yellowHigherMask, 9, false);
        setOtherBalls(stripedBalls, stripedBallsImg, blueLowerMask, blueHigherMask, 10, false);
        setRedBall(stripedBalls, stripedBallsImg, red1LowerMask, red1HigherMask, red2LowerMask, red2HigherMask, 11, false);
        setOtherBalls(stripedBalls, stripedBallsImg, purpleLowerMask, purpleHigherMask, 12, false);
        setOtherBalls(stripedBalls, stripedBallsImg, orangeLowerMask, orangeHigherMask, 13, false);
        setOtherBalls(stripedBalls, stripedBallsImg, greenLowerMask, greenHigherMask, 14, false);
        setOtherBalls(stripedBalls, stripedBallsImg, brownLowerMask, brownHigherMask, 15, false);

        detectedBalls.addAll(stripedBalls);

        System.gc();

        return detectedBalls;
    }

    public void setOtherBalls(List<Ball> balls, List<Mat> ballImgList,
                              Scalar lowerMask, Scalar higherMask, int id, boolean isSolid) {
        int indexOfBall = getIndexOfBall(ballImgList, lowerMask, higherMask);
        balls.get(indexOfBall).setId(id);
    }

    private int getIndexOfBall(List<Mat> ballImgList, Scalar lowerMask, Scalar higherMask) {
        Mat convertedImg = new Mat();
        double numberOfWhitePixels = 0;
        int indexOfBall = 0;

        for(int i = 0 ; i < ballImgList.size() ; i ++) {
            Imgproc.cvtColor(ballImgList.get(i), convertedImg, Imgproc.COLOR_BGR2HSV);
            Core.inRange(convertedImg, lowerMask, higherMask, temporaryBallImg);
            convertedImg.release();

            Imgproc.calcHist(Collections.singletonList(temporaryBallImg), channels, mask, temporaryHist, histSize ,ranges);
            temporaryBallImg.release();

            if(temporaryHist.get(1,0)[0] > numberOfWhitePixels) {
                indexOfBall = i;
                numberOfWhitePixels = temporaryHist.get(1,0)[0];
                temporaryHist.release();
            }
        }

        return indexOfBall;
    }

    public void setRedBall(List<Ball> balls, List<Mat> ballImgList,
                           Scalar lowerMask1, Scalar higherMask1,
                           Scalar lowerMask2, Scalar higherMask2,
                           int id, boolean isSolid) {

        double numberOfWhitePixels = 0;
        int indexOfBall = 0;

        indexOfBall = getIndexOfRedBall(ballImgList, lowerMask1, higherMask1,
                lowerMask2, higherMask2, numberOfWhitePixels, indexOfBall);

        balls.get(indexOfBall).setId(id);
    }

    private int getIndexOfRedBall(List<Mat> ballImgList, Scalar lowerMask1, Scalar higherMask1,
                                  Scalar lowerMask2, Scalar higherMask2, double numberOfWhitePixels,
                                  int indexOfBall) {

        Mat temporaryBallImg2 = new Mat();
        Mat destinationImg = new Mat();
        Mat convertedImage = new Mat();

        for(int i = 0 ; i < ballImgList.size() ; i ++) {
            Imgproc.cvtColor(ballImgList.get(i), convertedImage, Imgproc.COLOR_BGR2HSV);
            Core.inRange(convertedImage, lowerMask1, higherMask1, temporaryBallImg);
            Core.inRange(convertedImage, lowerMask2, higherMask2, temporaryBallImg2);
            convertedImage.release();

            Core.addWeighted(temporaryBallImg, 1.0, temporaryBallImg2, 1.0, 0.0, destinationImg);
            temporaryBallImg.release();
            temporaryBallImg2.release();

            Imgproc.calcHist(Collections.singletonList(destinationImg), channels, mask, temporaryHist, histSize ,ranges);
            destinationImg.release();

            if(temporaryHist.get(1,0)[0] > numberOfWhitePixels) {
                indexOfBall = i;
                numberOfWhitePixels = temporaryHist.get(1,0)[0];
                temporaryHist.release();
            }
        }

        return indexOfBall;
    }

    private void differentiateStripesAndSolids(List<Ball> detectedBalls, List<Rect> roiList) {
        List<Mat> planes = new ArrayList<>(3);
        Core.split(sourceImg, planes);

        Mat firstPlaneEqualized = new Mat();
        Mat secondPlaneEqualized = new Mat();

        Imgproc.equalizeHist(planes.get(0), firstPlaneEqualized);
        Imgproc.equalizeHist(planes.get(1), secondPlaneEqualized);
        planes.get(0).release();
        planes.get(1).release();

        Mat firstPlaneThreshold = new Mat();
        Mat secondPlaneThreshold = new Mat();

        Imgproc.threshold(firstPlaneEqualized, firstPlaneThreshold,200,255,Imgproc.THRESH_BINARY);
        Imgproc.threshold(secondPlaneEqualized, secondPlaneThreshold,200,255,Imgproc.THRESH_BINARY);
        firstPlaneEqualized.release();
        secondPlaneEqualized.release();

        List<Mat> listOfB = cropImage(roiList, firstPlaneThreshold);
        List<Mat> listOfG = cropImage(roiList, secondPlaneThreshold);
        firstPlaneThreshold.release();
        secondPlaneThreshold.release();

        Mat histB = new Mat();
        Mat histG = new Mat();

        for(int k = 0 ; k < listOfB.size() ; k ++) {
            Imgproc.calcHist(Collections.singletonList(listOfB.get(k)), channels, mask, histB, histSize ,ranges);
            Imgproc.calcHist(Collections.singletonList(listOfG.get(k)), channels, mask, histG, histSize ,ranges);
            listOfB.get(k).release();
            listOfG.get(k).release();

            if(histB.get(1,0)[0] > 3 * histG.get(1,0)[0]) {
                detectedBalls.get(k).setWhitePixels(histG.get(1,0)[0]);
            }else if(histG.get(1,0)[0] > 3 * histB.get(1,0)[0]) {
                detectedBalls.get(k).setWhitePixels(histB.get(1,0)[0]);
            }else {
                detectedBalls.get(k).setWhitePixels((histB.get(1, 0)[0] + histG.get(1, 0)[0])/2);
            }
        }

        histB.release();
        histG.release();

    }
}