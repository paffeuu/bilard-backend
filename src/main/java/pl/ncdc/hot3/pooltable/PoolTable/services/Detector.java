package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.ProjectProperties;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;

@Service
public class Detector {

	private final String EMPTY_TABLE_IMG = "src/main/resources/emptyTable.png";

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat sourceImg;
	private Mat outputImg;
	private Mat cannyImg;


	final double leftBand;
	final double rightBand;
	final double topBand;
	final double bottomBand;

	final int maxRadiusForBall = 22;
	final int minRadiusForBall = 16;
	final int minDistanceForBalls = 36;
	final int highThreshold = 105;
	final int ratio = 3;

	public Detector() {
		this.outputImg = new Mat();
		this.cannyImg = new Mat();

		double sourceWidth = 0;
		double sourceHeight = 0;

		try {
			sourceImg = Imgcodecs.imread(EMPTY_TABLE_IMG, Imgcodecs.IMREAD_COLOR);
			cannyImg = getEdges(sourceImg);

			sourceWidth = sourceImg.width();
			sourceHeight = sourceImg.height();


		} catch (DetectorException e) {
			LOGGER.error("Cannot calibrate table. Source image for empty table not found or broken.");
		} finally {
			leftBand = 175;
			rightBand = sourceWidth - 105;
			topBand = 350;
			bottomBand = sourceHeight - 300;
		}
	}

	public Mat getCannyImg() {
		return cannyImg;
	}

	public void setCannyImg(Mat cannyImg) {
		this.cannyImg = cannyImg;
	}

	public Mat getSourceImg() {
		return this.sourceImg;
	}

	public void setSourceImg(Mat sourceImg) {
		this.sourceImg = sourceImg;
	}

	public Mat getOutputImg() {
		return this.outputImg;
	}

	public void setOutputImg(Mat outputImg) {
		this.outputImg = outputImg;
	}

	public Mat detectBalls(Mat image) {
		// blur image
		Imgproc.blur(image, image, new Size(5, 5));

		// convert to hsv
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);

		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(image, planes);

		// canny - detect edges
		Mat edges = new Mat();
		Imgproc.Canny(planes.get(2), edges, highThreshold/ratio, highThreshold);

		// detect circles
		Mat circles = new Mat(); // contains balls coordinates
		int maxRadius = 22;
		int minRadius = 16;
		int minDistance = 36;
		Imgproc.HoughCircles(edges, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDistance,
				105, 12, minRadius, maxRadius);

		return circles;
	}

	private Mat getEdges(Mat source) throws DetectorException {
		Mat dst = new Mat();
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.blur(source, source, new Size(6,6));

			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			Core.split(source, layers);
			Imgproc.Canny(layers.get(1), dst, 50, 200, 3, false);

		} catch (NullPointerException e){
			throw new LinesDetectorException("Could not read source stream.", e);
		}

		return dst;
	}

	public Line findStickLine() throws DetectorException {

		Line tempLine = null;

		Mat substractedImg = new Mat();
		Mat linesP = getEdges(sourceImg);

		Core.subtract(linesP, cannyImg, substractedImg);

		Imgproc.HoughLinesP(substractedImg, linesP, 1, Math.PI/180, 50, 50, 10);

		for (int x = 0; x < linesP.rows(); x++){
			double line[] = linesP.get(x, 0);

			tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
			if (isPointInsideBand(tempLine.getBegin()) || isPointInsideBand(tempLine.getEnd())){
				return tempLine;
			}
		}

		return tempLine;
	}

	public Line getExtendedStickLine(Line stickLine){

		Line extendedLine = new Line();

		double Y = (stickLine.getBegin().y - stickLine.getEnd().y);
		double X = (stickLine.getBegin().x - stickLine.getEnd().x);

		if (X == 0){
			Point maxTop = new Point(stickLine.getBegin().x, 0);
			Point maxBot = new Point(stickLine.getBegin().x, sourceImg.height());

			extendedLine.setBegin(maxTop);
			extendedLine.setEnd(maxBot);
		} else if (Y == 0){
			Point maxLeft = new Point(0, stickLine.getBegin().y);
			Point maxRight = new Point(sourceImg.width(), stickLine.getBegin().y);

			extendedLine.setBegin(maxLeft);
			extendedLine.setEnd(maxRight);
		} else {
			double a = Y/X;
			double b = stickLine.getBegin().y - (a*stickLine.getBegin().x);

			Point maxTop = new Point(stickLine.getBegin().x, 0);
			maxTop.y = 0;
			maxTop.x = -(b / a);

			Point maxBot = new Point(stickLine.getBegin().x, sourceImg.height());
			maxBot.y = sourceImg.height();
			maxBot.x = (-b + sourceImg.height()) / a;

			Point maxLeft = new Point();
			maxLeft.x = 0;
			maxLeft.y = b;

			Point maxRight = new Point();
			maxRight.x = sourceImg.width();
			maxRight.y = a * sourceImg.width() + b;

			if (maxTop.x >= 0 && maxTop.x <= sourceImg.width()){
				extendedLine.setPoint(maxTop);
			}

			if (maxLeft.y >= 0 && maxLeft.y <= sourceImg.height()){
				extendedLine.setPoint(maxLeft);
			}

			if (maxBot.x >= 0 && maxBot.x <= sourceImg.width()){
				extendedLine.setPoint(maxBot);
			}

			if (maxRight.y >= 0 && maxRight.y <= sourceImg.height()){
				extendedLine.setPoint(maxRight);
			}

		}

		return extendedLine;
	}

	public ArrayList<Ball> createListOfBalls(Mat image) {
		int x,y,r;
		Mat circles = detectBalls(image);
		ArrayList<Ball> balls = new ArrayList<>();

		for (int i = 1; i < circles.cols(); i++) {
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


	private boolean isPointInsideBand(Point point){
		if (point.x > leftBand && point.x < rightBand) {
			if (point.y > topBand && point.y < bottomBand) {
				return true;
			}
		}
		return false;
	}
}
