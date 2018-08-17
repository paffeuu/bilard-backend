package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.io.File;
import java.io.FileNotFoundException;
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
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@ContextConfiguration(classes = {CueService.class, Properties.class})
@Service
public class Detector {

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat sourceImg;
	private Mat outputImg;
	private Mat cannyImg;

	final int maxRadiusForBall = 22;
	final int minRadiusForBall = 16;
	final int minDistanceForBalls = 36;
	final int highThreshold = 105;
	final int ratio = 3;

	private static Properties properties;
	private CueService cueService;

	@Autowired
	public Detector(
			CueService cueService,
			Properties properties
	) throws DetectorException {
		this.properties = properties;
		this.cueService = cueService;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.outputImg = new Mat();
		this.cannyImg = new Mat();

        String emptyTablePath = "";
		try {
		    emptyTablePath = properties.getFullPath("emptyTable.png");
            System.out.println("==================================================================");
            System.out.println(emptyTablePath);
            sourceImg = Imgcodecs.imread(emptyTablePath, Imgcodecs.IMREAD_COLOR);

            System.out.println("Source " + sourceImg.size());
            cannyImg = getEdges(sourceImg);

            System.out.println("canny" + cannyImg.size());
            System.out.println("==================================================================");
		}
		catch (FileNotFoundException e) {
            LOGGER.error("Empty table for callibrate not founded.");
		}
		catch (DetectorException e) {
			LOGGER.error("Cannot calibrate table. Source image for empty table not found or broken.");
		} finally {
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

	public Mat detectBalls() throws BallsDetectorException {
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

	private Mat filterCircles(Mat allCircles) throws BallsDetectorException {

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

		return filteredCircles;
	}

	public Line findStickLine() throws MissingCueLineException, DetectorException, LineServiceException {

		List <Line> linesList = getInnerLines();
		return cueService.findStickLine(linesList);

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
			if (properties.isPointInsideBand(tempLine.getBegin()) || properties.isPointInsideBand(tempLine.getEnd())){
				linesList.add(tempLine);
			}
		}

		return linesList;
	}

	private Mat getEdges(Mat source) throws DetectorException {
		Mat dst = new Mat();
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.blur(source, source, new Size(6,6));

			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			Core.split(source, layers);
			Imgproc.Canny(layers.get(2), dst, 50, 200, 3, false);

		} catch (Exception e){
			throw new LinesDetectorException("Could not read source stream.", e);
		}

		return dst;
	}

	public List<Line> getPredictions() throws CueServiceException, LineServiceException {
		List <Line> predictions = new ArrayList<>();

		try {
			Line cue = findStickLine();
			predictions.add(cue);

			for (int i = 0; i < properties.getPredictionDepth(); i++){
				Line pred = cueService.predictTrajectoryAfterBump(predictions.get(i));
				predictions.add(pred);
			}

		} catch (MissingCueLineException e) {
			LOGGER.warn("Could not find stick, predictions canceled.");
		} finally {
			if (predictions.size() > 1)
				predictions.subList(1, predictions.size() -1);

			return predictions;
		}
	}

	public ArrayList<Ball> createListOfBalls() throws BallsDetectorException {
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
//
////	public boolean isPointInsideBand(Point point){
////		return isPointInsideBand(point);
////	}
//
//

	public double[] convertMatToArray(Mat mat) {
		double[] data = null;
		try {
			int size = (int) mat.total() * mat.channels();
			data = new double[size];
			mat.get(0, 0, data);

			return null;
		} catch (Exception e) {
			LOGGER.info("Can not convert mat to array. Returned null");
		} finally {
			return data;
		}
	}

	public Line createTargetLine(Line line, ArrayList<Ball> balls, boolean isCue) throws LineServiceException {
		Ball collision = cueService.stopLineAtFirstBall(line, balls, isCue);

		if (null != collision) {
			return cueService.findBallColisionLine(line, collision);
		}

		return null;
	}
}


