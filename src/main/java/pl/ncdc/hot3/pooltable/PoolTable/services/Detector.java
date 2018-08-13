package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.security.auth.PrivateCredentialPermission;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.ProjectProperties;
import pl.ncdc.hot3.pooltable.PoolTable.PropertiesReader;
import pl.ncdc.hot3.pooltable.PoolTable.WritePropertiesFile;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.PropertiesReaderException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;

/* obsuga obrazu wejsciowego w formacie .jpg
 * 
 * Convert to Mat
 * 	BufferedImage image = ImageIO.read(input);         
 	byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();            
 	Mat img = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
 	img.put(0, 0, data); 
 	
 	convert to grey
 	Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
 	
 	
 	Imgcodecs.imwrite("C:\\Files\\input.jpg", img);
 	File input = new File("C:\\Files\\pool1.png");
 *
 */

@Service
public class Detector {

	static final String PROPERTIES_FILE = "detector.properties";
	private final String EMPTY_TABLE_IMG = "src/main/resources/emptyTable.png";

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat sourceImg;
	private Mat outputImg;
	private Mat cannyImg;

	/* final */ int leftBand = 0;
	int rightBand = 0;
	int topBand = 0;
	int bottomBand = 0;

	int maxRadiusForBall = 0;
	int minRadiusForBall = 0;
	int minDistanceForBalls = 0;
	// ??????????????????????????????
	int highThreshold = 105;
	int ratio = 3;

	private PropertiesReader properties;

	public Detector() {
		this.outputImg = new Mat();
		this.cannyImg = new Mat();

		double sourceWidth = 0;
		double sourceHeight = 0;

		try {
			this.properties = new PropertiesReader(PROPERTIES_FILE);

			sourceImg = Imgcodecs.imread(ProjectProperties.EMPTY_TABLE_IMG, Imgcodecs.IMREAD_COLOR);
			cannyImg = getEdges(sourceImg);

			sourceWidth = sourceImg.width();
			sourceHeight = sourceImg.height();
		} catch (DetectorException e) {
			LOGGER.error("Cannot calibrate table. Source image for empty table not found or broken.");
		} catch (PropertiesReaderException e) {
			LOGGER.error("Cannot read detector.properties. File not found or broken");
		} /*
			 * finally { //?????????????????????????? leftBand = 175; rightBand =
			 * sourceWidth - 105; topBand = 350; bottomBand = sourceHeight - 300; }
			 * //???????????????/////////
			 */

		try {
			leftBand = properties.getIntProperty("table.band.left");
			rightBand = properties.getIntProperty("table.band.right");
			topBand = properties.getIntProperty("table.band.top");
			bottomBand = properties.getIntProperty("table.band.bottom");

			maxRadiusForBall = properties.getIntProperty("ball.maxRadius");
			minRadiusForBall = properties.getIntProperty("ball.minRadius");
			minDistanceForBalls = properties.getIntProperty("ball.minDistance");

			highThreshold = properties.getIntProperty("canny.highThreshold");
			ratio = properties.getIntProperty("canny.ratio");
		} catch (PropertiesReaderException e) {
			LOGGER.error("Could not property from " + PROPERTIES_FILE);
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
		Imgproc.blur(image, image, new Size(5, 5)); // was 1,1

		// convert to HSV
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);

		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(image, planes);

		// canny - detect edges
		Mat edges = new Mat();
		Imgproc.Canny(planes.get(2), edges, highThreshold / ratio, highThreshold);

		// detect circles from the whole image
		Mat allCircles = new Mat(); // contains balls coordinates
		Imgproc.HoughCircles(edges, allCircles, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDistanceForBalls, 105, 12,
				minRadiusForBall, maxRadiusForBall);

		// filter circles from the table
		Mat filteredCircles = filterCircles(allCircles);

		return filteredCircles;
	}

	// filter circles positioned only on the table
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
					filteredCircles.put(0, j, new double[] { x, y, r });
					matList.set(0, filteredCircles);
				} else {
					// merge horizontally filteredCircles with newMat and save to filteredCircles
					newMat.put(0, 0, new double[] { x, y, r });
					matList.set(1, newMat);
					Core.hconcat(matList, filteredCircles);
					matList.set(0, filteredCircles);
				}

				j++;
			}
		}

		return filteredCircles;
	}

	// crop image into pieces specified by roi
	public List<Mat> cropImage(List<Rect> roi, Mat image) {
		List<Mat> crops = new ArrayList<>();

		for (int i = 0; i < roi.size(); i++) {
			crops.add(new Mat(image, roi.get(i)));
		}
		return crops;
	}

	// get balls region of interest
	public List<Rect> getBallsROI(double[] circles, Mat image) {
		List<Rect> roiList = new ArrayList<>();

		for (int i = 0; i < circles.length; i += 3) {
			double x = circles[i];
			double y = circles[i + 1];
			double r = circles[i + 2];
			Point topLeft = new Point(x - r, y - r);
			Point bottomRight = new Point(x + r, y + r);

			roiList.add(new Rect(topLeft, bottomRight));
		}

		return roiList;
	}
	
	public double[] getMeanColor(List<Mat> images) {
		double[] meanArray = new double[images.size()];

		for (int i = 0; i < images.size(); i++) {

			MatOfDouble mean = new MatOfDouble();
			Core.meanStdDev(images.get(i), mean, new MatOfDouble());
			meanArray[i]=mean.get(0, 0)[0];
		}
		
		return meanArray;
	}

	// add ball id to its coordinates
	public double[] getBallsId(double[] circles, Mat image) {

		// crop image to balls
		List<Rect> roiList = getBallsROI(circles, image);
		List<Mat> ballImgList = cropImage(roiList, image);

		// compute mean value of color
		double[] mean = getMeanColor(ballImgList);
		
		// assign id by color

		double[] id = null;
		return id;
	}

	/*
	 * public Ball detectWhiteBall() { System.out.println("White ball");
	 * 
	 * // blur image Imgproc.blur(sourceImg, outputImg, new Size(1, 1));
	 * 
	 * // convert to HSV Imgproc.cvtColor(outputImg, outputImg,
	 * Imgproc.COLOR_BGR2HSV);
	 * 
	 * // split into planes List<Mat> planes = new ArrayList<>(3);
	 * Core.split(outputImg, planes);
	 * 
	 * // canny - detect edges Mat edges = new Mat(); int highThreshold = 105; int
	 * ratio = 3;
	 * 
	 * // wykrycie białych obiektów Mat mask = new Mat(); int colorSensitivity =
	 * 32;// 35 int greySenitivity = 255; Core.inRange(outputImg, new Scalar(33, 0,
	 * 255 - greySenitivity), new Scalar(90, colorSensitivity, 255), mask);
	 * Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\mask.jpg", mask);
	 * 
	 * Imgproc.Canny(mask, edges, highThreshold / ratio, highThreshold);
	 * Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\canny.jpg", edges);
	 * 
	 * // detect circles Mat allCircles = new Mat(); // contains balls coordinates
	 * int maxRadius = 22; int minRadius = 17; int minDistance = 36;
	 * 
	 * Imgproc.HoughCircles(edges, allCircles, Imgproc.CV_HOUGH_GRADIENT, 1.0,
	 * minDistance, 120, 10, minRadius, maxRadius);
	 * 
	 * Mat whiteBall = filterCircles(allCircles);
	 * 
	 * // return ball object double[] ballData = whiteBall.get(0, 0); Ball ball =
	 * new Ball(0, ballData[0], ballData[1], ballData[2]); return ball;
	 * 
	 * }
	 * 
	 * private Mat makeMask() {
	 * 
	 * // wykrycie białych obiektów Mat mask = new Mat(); int colorSensitivity =
	 * 32;// 35 int greySenitivity = 255; Core.inRange(outputImg, new Scalar(33, 0,
	 * 255 - greySenitivity), new Scalar(90, colorSensitivity, 255), mask);
	 * Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\mask.jpg", mask);
	 * 
	 * return mask; }
	 */

	private Mat getEdges(Mat source) throws DetectorException {
		Mat dst = new Mat();
		List<Mat> layers = new ArrayList<>();

		try {
			Imgproc.blur(source, source, new Size(6, 6));

			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			Core.split(source, layers);
			Imgproc.Canny(layers.get(1), dst, 50, 200, 3, false);

		} catch (NullPointerException e) {
			throw new LinesDetectorException("Could not read source stream.", e);
		}

		return dst;
	}

	public Line findStickLine() throws DetectorException {

		Line tempLine = null;

		Mat substractedImg = new Mat();
		Mat linesP = getEdges(sourceImg);

		Core.subtract(linesP, cannyImg, substractedImg);

		Imgproc.HoughLinesP(substractedImg, linesP, 1, Math.PI / 180, 50, 50, 10);

		for (int x = 0; x < linesP.rows(); x++) {
			double line[] = linesP.get(x, 0);

			tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
			if (isPointInsideBand(tempLine.getBegin()) || isPointInsideBand(tempLine.getEnd())) {
				return tempLine;
			}
		}

		return tempLine;
	}

	public Line getExtendedStickLine(Line stickLine) {

		Line extendedLine = new Line();

		double Y = (stickLine.getBegin().y - stickLine.getEnd().y);
		double X = (stickLine.getBegin().x - stickLine.getEnd().x);

		if (X == 0) {
			Point maxTop = new Point(stickLine.getBegin().x, 0);
			Point maxBot = new Point(stickLine.getBegin().x, sourceImg.height());

			extendedLine.setBegin(maxTop);
			extendedLine.setEnd(maxBot);
		} else if (Y == 0) {
			Point maxLeft = new Point(0, stickLine.getBegin().y);
			Point maxRight = new Point(sourceImg.width(), stickLine.getBegin().y);

			extendedLine.setBegin(maxLeft);
			extendedLine.setEnd(maxRight);
		} else {
			double a = Y / X;
			double b = stickLine.getBegin().y - (a * stickLine.getBegin().x);

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

			if (maxTop.x >= 0 && maxTop.x <= sourceImg.width()) {
				extendedLine.setPoint(maxTop);
			}

			if (maxLeft.y >= 0 && maxLeft.y <= sourceImg.height()) {
				extendedLine.setPoint(maxLeft);
			}

			if (maxBot.x >= 0 && maxBot.x <= sourceImg.width()) {
				extendedLine.setPoint(maxBot);
			}

			if (maxRight.y >= 0 && maxRight.y <= sourceImg.height()) {
				extendedLine.setPoint(maxRight);
			}

		}

		return extendedLine;
	}

	public ArrayList<Ball> createListOfBalls(Mat image) {

		Mat circles = detectBalls(image);

		// write circles coordinates into an array
		double[] data = convertMatToArray(circles);

		// get IDs
		double[] id = getBallsId(data, image);

		ArrayList<Ball> balls = new ArrayList<>();

		int x, y, r;
		for (int i = 0; i < data.length; i += 3) {
			// read ball coordinates
			x = (int) data[i];
			y = (int) data[i + 1];
			r = (int) data[i + 2];

			Ball ball = new Ball(i, x, y, r);
			balls.add(ball);
		}

		return balls;
	}

	private boolean isPointInsideBand(Point point) {
		if (point.x > leftBand && point.x < rightBand) {
			if (point.y > topBand && point.y < bottomBand) {
				return true;
			}
		}
		return false;
	}

	public double[] convertMatToArray(Mat mat) {
		int size = (int) mat.total() * mat.channels();
		double[] data = new double[size];
		mat.get(0, 0, data);

		return data;
	}
}
