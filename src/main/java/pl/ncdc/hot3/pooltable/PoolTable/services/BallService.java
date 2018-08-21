package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DrawerException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Service
public class BallService {

    private Properties properties;

    @Autowired
    public BallService(
            Properties properties
    ) {
        this.properties = properties;
    }

    public Mat filterCircles(Mat allCircles) throws BallsDetectorException {

        Mat filteredCircles = new Mat(1, 1, CvType.CV_64FC3); // output Mat
        Mat newMat = new Mat(1, 1, CvType.CV_64FC3); // merged new column
        List<Mat> matList = new ArrayList<>(); // 2-element list for merging in Core.hconcat
        matList.add(null);
        matList.add(null);

        // conversion to use type double data
        allCircles.convertTo(allCircles, CvType.CV_64FC3);

        // write circles coordinates into an array
        double[] data = convertMatToArray(allCircles);

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
        newMat.release();

        return filteredCircles;
    }

    public ArrayList<Ball> convertMatToListOfBalls(Mat circles) throws BallsDetectorException {
        int x,y,r;

        ArrayList<Ball> balls = new ArrayList<>();

        for (int i = 0; i < circles.cols(); i++) {
            // read ball coordinates
            double[] data = circles.get(0, i);

            x = (int) data[0];
            y = (int) data[1];
            r = (int) data[2];

            Ball ball = new Ball(i,x,y,r);
            balls.add(ball);
        }

        return balls;
    }

    public double[] convertMatToArray(Mat mat) {

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

    public List<Mat> cropImage(List<Rect> roi, Mat image) {
        List<Mat> crops = new ArrayList<>();

        for(Rect rect : roi) {
            crops.add(new Mat(image, rect));
        }
        return crops;
    }

    public List<Rect> getBallsROI(double[] circles) {
        List<Rect> roiList = new ArrayList<>();

        for (int i = 0; i < circles.length; i += 3) {
            double x = circles[i];
            double y = circles[i + 1];
            double r = 21;

            Point topLeft = new Point(x - r, y - r);
            Point bottomRight = new Point(x + r, y + r);

            roiList.add(new Rect(topLeft, bottomRight));
        }

        return roiList;
    }

    public Mat detectBalls(Mat image) throws BallsDetectorException {
        Mat blurredImage = new Mat();
        // blur convertedImage
        Imgproc.blur(image, blurredImage, new Size(5, 5));

        Mat convertedImage = new Mat();
        // convert to hsv
        Imgproc.cvtColor(blurredImage, convertedImage, Imgproc.COLOR_BGR2HSV);
        blurredImage.release();

        // split into planes
        List<Mat> planes = new ArrayList<>(3);
        Core.split(convertedImage, planes);
        convertedImage.release();

        Mat destinationImage = new Mat();
        // detect circles
        Imgproc.HoughCircles(planes.get(2), destinationImage, Imgproc.CV_HOUGH_GRADIENT, 1.0, properties.getBallMinDistance(),
                30, 15, properties.getBallMinRadius(), properties.getBallMaxRadius());
        planes.clear();

        return this.filterCircles(destinationImage);
    }

    public ArrayList<Ball> createListOfBalls(Mat sourceImg) throws BallsDetectorException {
        Mat circles = detectBalls(sourceImg);

        List<Rect> roiList = getBallsROI(convertMatToArray(circles));
        List<Mat> ballImgList = cropImage(roiList, sourceImg);

        ArrayList<Ball> detectedBalls = convertMatToListOfBalls(circles);

        List<Mat> planes = new ArrayList<>();

        Core.split(sourceImg, planes);

        Mat plane0Equalized = new Mat();
        Mat plane1Equalized = new Mat();

        Imgproc.equalizeHist(planes.get(0), plane0Equalized);
        Imgproc.equalizeHist(planes.get(1), plane1Equalized);
        planes.get(0).release();
        planes.get(1).release();

        Mat plane0Threshold = new Mat();
        Mat plane1Threshold = new Mat();

        Imgproc.threshold(plane0Equalized, plane0Threshold,200,255,Imgproc.THRESH_BINARY);
        Imgproc.threshold(plane1Equalized, plane1Threshold,200,255,Imgproc.THRESH_BINARY);
        plane0Equalized.release();
        plane1Equalized.release();

        List<Mat> listOfB = cropImage(roiList, plane0Threshold);
        List<Mat> listOfG = cropImage(roiList, plane1Threshold);
        roiList.clear();

        Mat histB = new Mat();
        Mat histG = new Mat();

        MatOfFloat ranges = new MatOfFloat(0f,256f);
        MatOfInt channels = new MatOfInt(0);
        MatOfInt histSize = new MatOfInt(2);
        Mat mask = new Mat();

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
        ranges.release();
        channels.release();
        histSize.release();
        mask.release();
        histB.release();
        histG.release();

        int stripedId = 8;
        int solidId = 0;
        for(Ball ball : detectedBalls) {
            if((ball.getWhitePixels()*100)/1764 >= 16) {
                ball.setId(stripedId);
                stripedId++;
            } else {
                ball.setId(solidId);
                solidId++;
            }
        }

        return detectedBalls;
    }
}
