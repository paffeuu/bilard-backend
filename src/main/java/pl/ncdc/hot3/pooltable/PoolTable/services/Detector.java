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

	private static Properties properties;
	private CueService cueService;
	private BallService ballService;

	@Autowired
	public Detector(
			CueService cueService,
			Properties properties,
			BallService ballService
	) throws DetectorException {
		this.ballService = ballService;
		this.properties = properties;
		this.cueService = cueService;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.outputImg = new Mat();
		this.cannyImg = new Mat();

		try {
			sourceImg = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), Imgcodecs.IMREAD_COLOR);
			cannyImg = getEdges(sourceImg);

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (DetectorException e) {
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

	public ArrayList<Ball> createListOfBalls() throws BallsDetectorException, DrawerException {
		Mat circles = ballService.detectBalls(sourceImg);

		List<Rect> roiList = ballService.getBallsROI(ballService.convertMatToArray(circles));
		List<Mat> ballImgList = ballService.cropImage(roiList, sourceImg);

		return ballService.createListOfBalls(circles, sourceImg, ballImgList, roiList);
	}

	public Line findStickLine() throws MissingCueLineException, DetectorException, LineServiceException {

		List <Line> linesList = getInnerLines();
		return cueService.findStickLine(linesList);

	}

	private List<Line> getInnerLines() throws DetectorException {
		Line tempLine;

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
			Imgproc.blur(source, dst, new Size(6,6));

			Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2HSV);
			Core.split(dst, layers);
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
}


