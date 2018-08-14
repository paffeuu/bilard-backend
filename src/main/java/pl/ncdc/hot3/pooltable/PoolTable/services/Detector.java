package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import pl.ncdc.hot3.pooltable.PoolTable.ProjectProperties;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@ContextConfiguration(classes = {Properties.class})
@Service
public class Detector {

	private final String EMPTY_TABLE_IMG = "src/main/resources/emptyTable.png";

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat sourceImg;
	private Mat outputImg;
	private Mat cannyImg;

	final int maxRadiusForBall = 22;
	final int minRadiusForBall = 16;
	final int minDistanceForBalls = 36;
	final int highThreshold = 105;
	final int ratio = 3;
	final int cueThickness = 17;

	@Autowired
	private static Properties properties;

	@Autowired
	private CueService cueService;

	@Autowired
	private LineService lineService;

	public Detector() {
		this.properties = properties;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

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

	public Mat detectBalls() {
		Mat image = sourceImg.clone();

		// blur image
		Imgproc.blur(image, image, new Size(5, 5));

		// convert to hsv
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);

		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(image, planes);



		// detect circles
		Mat circles = new Mat(); // contains balls coordinates
		Imgproc.HoughCircles(planes.get(2), circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDistanceForBalls,
				30, 15, minRadiusForBall, maxRadiusForBall);

		return filterCircles(circles);
	}

	private Mat filterCircles(Mat allCircles) {

		Mat filteredCircles = new Mat(1, 1, CvType.CV_64FC3); // output Mat
		Mat newMat = new Mat(1, 1, CvType.CV_64FC3); // merged new column
		List<Mat> matList = new ArrayList<>(); // 2-element list for merging in Core.hconcat
		matList.add(null);
		matList.add(null);

		// conversion to use type double data
		allCircles.convertTo(allCircles, CvType.CV_64FC3);

		// write circles coordinates into an array
		double[] data = convertMatToArray(allCircles);

		// filter circles
		int j = 0;
		double x, y, r;
		for (int i = 0; i < data.length; i += 3) {

			// read coordinates
			x = data[i];
			y = data[i + 1];
			r = data[i + 2];

			// check if they are within table boundaries
			if (isPointInsideBand(new Point(x, y))) {

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





	private Mat getEdges(Mat source) throws DetectorException {
		Mat dst = new Mat();
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.blur(source, source, new Size(6,6));

			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			Core.split(source, layers);
			Imgproc.Canny(layers.get(2), dst, 50, 200, 3, false);

		} catch (NullPointerException e){
			throw new LinesDetectorException("Could not read source stream.", e);
		}

		return dst;
	}

	private List<Line> getInnerLines() throws DetectorException {
		Line tempLine = null;

		Mat substractedImg = new Mat();
		Mat linesP = getEdges(sourceImg);

		Core.subtract(linesP, cannyImg, substractedImg);

		Imgproc.HoughLinesP(substractedImg, linesP, 1, Math.PI/180, 70, 50, 10);

		List <Line> linesList = new ArrayList<>();

		for (int x = 0; x < linesP.rows(); x++){
			double line[] = linesP.get(x, 0);

			tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
			if (isPointInsideBand(tempLine.getBegin()) || isPointInsideBand(tempLine.getEnd())){
				linesList.add(tempLine);
			}
		}

		return linesList;
	}

	public Line findStickLine() throws DetectorException, CueServiceException {

		List <Line> linesList = getInnerLines();
		Line cueLine = null;

		double dist;
		double a1, a2, parallelTolerance = 0.2;

		double minDistance = 50;
		outerloop:
		for (int i = 0; i < linesList.size() - 1; i++){
			for (int j = 0; j < linesList.size(); j++){
				if (i != j) {

					a1 = lineService.calcCoordinate_A(linesList.get(i));
					a2 = lineService.calcCoordinate_A(linesList.get(j));

					if (Math.abs(a1 - a2) < parallelTolerance) {
						dist = getDistanceBetweenLines(linesList.get(i), linesList.get(j));
						if (dist < minDistance) {
							cueLine = lineService.getDirectedLine(linesList.get(i), linesList.get(j));

							break outerloop;
						}
					}

				}
			}
		}

		return cueLine;
	}



	public ArrayList<Ball> createListOfBalls() {
		int x,y,r;
		Mat circles = detectBalls();
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

	public boolean isPointInsideBand(Point point){
		return isPointInsideBand(point, new Properties());
	}

	public static boolean isPointInsideBand(Point point, Properties properties){
		if (point.x >= properties.getTableBandLeft() - 5 && point.x <= properties.getTableBandRight() + 5) {
			if (point.y >= properties.getTableBandTop() - 5 && point.y <= properties.getTableBandBottom() + 5) {
				return true;
			}
		}
		return false;
	}

	private double getDistanceBetweenLines(Line line1, Line line2) {

		double begin2begin = getDistanceBetweenPoints(line1.getBegin(), line2.getBegin());
		double begin2end = getDistanceBetweenPoints(line1.getBegin(), line2.getEnd());
		double end2begin = getDistanceBetweenPoints(line1.getEnd(), line2.getBegin());
		double end2end = getDistanceBetweenPoints(line1.getEnd(), line2.getEnd());

		double min1 = getMinWithNoFirst(0, begin2begin, begin2end, end2begin, end2end);
		double min2 = getMinWithNoFirst(min1, begin2begin, begin2end, end2begin, end2end);

		return ((min1 + min2)/2);
	}

	private double getDistanceBetweenPoints(Point point1, Point point2) {
		return Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2));
	}

	private double getMinWithNoFirst(double discardThisMinValue, double ... values){
		double temp = Double.MAX_VALUE;
		for (int i = 0; i < values.length; i++){
			if (values[i] < temp && values[i] != discardThisMinValue) {
				temp = values[i];
			}
		}
		return temp;
	}

	public double[] convertMatToArray(Mat mat) {
		int size = (int) mat.total() * mat.channels();
		double[] data = new double[size];
		mat.get(0, 0, data);

		return data;
	}
}


