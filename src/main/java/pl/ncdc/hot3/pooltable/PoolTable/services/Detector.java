package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

@ContextConfiguration(classes = {CueService.class, Properties.class})
@Service
public class Detector {

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat emptyTableImage;
	private Mat sourceImg;
	private Mat outputImg;
	private Mat cannyImg;

	private static Properties properties;
	private CueService cueService;
	private BallService ballService;
	private LineService lineService;

	@Autowired
	public Detector(
			CueService cueService,
			Properties properties,
			BallService ballService,
			LineService lineService
	) {
		Detector.properties = properties;
		this.ballService = ballService;
		this.cueService = cueService;
		this.lineService = lineService;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.cannyImg = new Mat();



		try {
			sourceImg = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), Imgcodecs.IMREAD_COLOR);
			emptyTableImage = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), Imgcodecs.IMREAD_COLOR);

			cannyImg = getEdges(sourceImg);
		} catch (FileNotFoundException e) {
			LOGGER.error("File with empty table not founded.");
		} catch (DetectorException e) {
			LOGGER.warn("Cannot make edges for empty source image.");
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

	public ArrayList<Ball> createListOfBalls() throws BallsDetectorException {
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
		Mat linesP = getEdges(sourceImg.clone());

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

	public List<Line> getPredictions(Line cueLine) throws CueServiceException, LineServiceException {
		List <Line> predictions = new ArrayList<>();

		if (cueLine != null) {
			predictions.add(cueLine);
			for (int i = 0; i < properties.getPredictionDepth(); i++){
				Line pred = cueService.predictTrajectoryAfterBump(predictions.get(i));
				predictions.add(pred);
			}
		}

		return predictions;
	}

    public Line createTargetLine(Line line, ArrayList<Ball> balls, boolean isCue) throws LineServiceException {
        Ball collision = getCollisionBall(line, balls, isCue);

        if (null != collision) {
            return cueService.findBallCollisionLine(line, collision);
        }

        return null;
    }

	public Ball getCollisionBall(Line line, ArrayList<Ball> balls, boolean skipFirst) {
		double counter = 0;

		for (Ball ball : balls) {
			double distance = cueService.calculateDistanceBetweenPointAndLine(new Point(ball.getX(), ball.getY()), line);

			if (distance <= ball.getRadius() * 2) {
				++counter;

				if (!skipFirst || 2 == counter) {
					return ball;
				}
			}
		}

		return null;
	}

	public Line refactorCueLine(Line line, Ball ball) throws LineServiceException {
		double distance = cueService.calculateDistanceBetweenPointAndLine(new Point(ball.getX(), ball.getY()), line);
		double[] coordinates = cueService.calcAllCoordinate(line);
		double[] newCoordinates = {coordinates[0], coordinates[1], coordinates[2] + distance};
		double A = newCoordinates[0];
		double B = newCoordinates[1];
		double C = newCoordinates[2];

		return lineService.getExtendedStickLineForOneSide(
				new Line(
						new Point(
								ball.getX(),
								ball.getY()
						),
						new Point(
								line.getEnd().x,
								(-C - A * (line.getEnd().x)) / B
						)
				)
		);
	}
}


