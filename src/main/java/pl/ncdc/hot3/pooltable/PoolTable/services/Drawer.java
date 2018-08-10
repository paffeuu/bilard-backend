package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;

@Service
public class Drawer {

    @Autowired
    private Detector detector;

    public Mat drawBalls(Mat img) {
        Mat detectedBalls = detector.detectBalls(img.clone());

        int x,y,r;
        int leftBand = 175;
        int rightBand = img.width() - 105;
        int topBand = 350;
        int bottomBand = img.height() - 300;

        for (int i = 0; i < detectedBalls.cols(); i++) {
            // read ball coordinates
            double[] data = detectedBalls.get(0, i);

            x = (int) data[0];
            y = (int) data[1];
            r = (int) data[2];
            if((x > leftBand && x < rightBand) && (y > topBand && y < bottomBand)) {
                Point center = new Point(x, y);

                // draw circle center
                Imgproc.circle(img, center, 3, new Scalar(0, 255, 0), -1);

                // draw circle outline
                Imgproc.circle(img, center, r, new Scalar(0, 0, 255), 1);
            }
        }

        return img;
    }

    public Mat drawExtendedCue(Mat img, pl.ncdc.hot3.pooltable.PoolTable.model.Line line) {
        Imgproc.line(img, line.getBegin(), line.getEnd(), new Scalar(155,155,155), 4);
        return img;
    }

    public Mat draw(Mat img, Line line) throws DetectorException {
        Mat balls = drawBalls(img);
        Mat extendedCue = drawExtendedCue(img, line);
        Mat all = new Mat();
        Core.add(balls, extendedCue, all);
        return all;
    }
}