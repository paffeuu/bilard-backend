package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.io.FileNotFoundException;
import java.util.*;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.ConfigurableProperties;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;


@Service
public class Detector {

    final int MIN_LENGTH_FOR_INNER_LINES = 50;
    final int MAX_LINE_GAP_FOR_INNER_LINES = 10;
    final int TRESHOLD_FOR_INNER_LINES = 70;

    final int TRESHOLD_FOR_SOURCE_IMG = 140;
    final int TRESHOLD_FOR_CANNY_FIRST = 100;
    final int TRESHOLD_FOR_CANNY_SECOND = 40;


	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat emptyTableImage;
	private Mat sourceImg;
	private Mat outputImg;

	private CueService cueService;
	private BallService ballService;
	private PathService pathService;
	private BandsService bandsService;
	private Properties properties;
	private ConfigurableProperties configurableProperties;
	private LineService lineService;

	private List<Line> debugDetectedLines;
	private Line debugAverageLine;

	public Line debugPerpendicular;
	public Point debugLineEndPoint;

	@Autowired
	public Detector(
			CueService cueService,
			BallService ballService,
			PathService pathService,
			BandsService bandsService,
			Properties properties,
            ConfigurableProperties configurableProperties,
            LineService lineService
			) {

		this.ballService = ballService;
		this.cueService = cueService;
		this.properties = properties;
		this.configurableProperties = configurableProperties;
		this.pathService = pathService;
		this.bandsService = bandsService;
        this.lineService = lineService;

		this.debugDetectedLines = new ArrayList<>();
		this.debugAverageLine = null;

		try {
			sourceImg = Imgcodecs.imread(pathService.getFullPath("emptyTable.png"), CvType.CV_64F);

			emptyTableImage = Imgcodecs.imread(pathService.getFullPath("emptyTable.png"), CvType.CV_64F);
			emptyTableImage = prepereEmptyTableForSubs(emptyTableImage.clone());

		} catch (FileNotFoundException e) {
			LOGGER.error("File with empty table not founded.");
		} catch (DetectorException e) {
			LOGGER.warn("Cannot make edges for empty source image.");
		}

	}

	public Mat getSourceImg() {
		return this.sourceImg;
	}

	public void setSourceImg(Mat sourceImg) {
		this.sourceImg = sourceImg;
	}

	public Mat getEmptyTableImage() {
		return emptyTableImage;
	}

	public Mat getOutputImg() {
		return this.outputImg;
	}

	public void setOutputImg(Mat outputImg) {
		this.outputImg = outputImg;
	}

	public List<Ball> createListOfBalls() {

		// detect all balls on image, it returns Mat with x,y,r
		Mat detectedBalls = detectBalls();

		// filter circles to get only those inside bands
		Mat filteredBalls = filterCircles(detectedBalls);

		// Create list of rectangles around detected balls
		List<Rect> roiList = getBallsROI(convertMatToArray(filteredBalls));

		// List of current detected balls
		List<Ball> ballList = ballService.createListOfBalls(filteredBalls, sourceImg.clone(), roiList);

		// List of stabile with previous static balls
		ballList = ballService.stabilize(ballList);

        // Sort list of balls by id
        Collections.sort(ballList);

		return ballList;
	}

	private Mat detectBalls() {
		Mat blurredImage = new Mat();
		Mat convertedTypeImage = new Mat();
		Mat destinationImage = new Mat();
		Size blurSize = new Size(5, 5);

		// blur convertedImage
		Imgproc.blur(sourceImg, blurredImage, blurSize);

		// convert to hsv
		Imgproc.cvtColor(blurredImage, convertedTypeImage, Imgproc.COLOR_BGR2HSV);
		blurredImage.release();

		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(convertedTypeImage, planes);
		convertedTypeImage.release();

		// detect circles
		Imgproc.HoughCircles(planes.get(2), destinationImage, Imgproc.CV_HOUGH_GRADIENT, 1.0,
				properties.getBallMinDistance(), properties.getHoughCirclesParam1(),
				properties.getHoughCirclesParam2(), properties.getBallMinRadius(), properties.getBallMaxRadius());

		for (Mat mat: planes) {
			mat.release();
		}
		planes.clear();

		return destinationImage;
	}

	private Mat filterCircles(Mat allCircles) {

		// output mat
		Mat filteredCircles = new Mat(1, 1, CvType.CV_64FC3);

		// merged new column
		Mat newMat = new Mat(1, 1, CvType.CV_64FC3);

		// 2-element list for merging in Core.hconcat
		List<Mat> matList = new ArrayList<>();
		matList.add(null);
		matList.add(null);

		// conversion to use type double data
		Mat convertedAllCircles = new Mat();
		allCircles.convertTo(convertedAllCircles, CvType.CV_64FC3);

		// write circles coordinates into an array
		double[] data;
		data = convertMatToArray(convertedAllCircles);

		// filter circles
		double x, y, r;

		for (int i = 0, j = 0; i < data.length; i += 3) {

			// read coordinates
			x = data[i];
			y = data[i + 1];
			r = data[i + 2];

			// check if they are within table boundaries
			if (bandsService.isPointInsideBand(new Point(x, y))) {
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

	private List<Rect> getBallsROI(double[] circles) {
		double x, y, r;
		Point topLeft = new Point();
		Point bottomRight = new Point();

		List<Rect> roiList = new ArrayList<>();

		for (int i = 0; i < circles.length; i += 3) {
			x = circles[i];
			y = circles[i + 1];
			r = properties.getBallExpectedRadius();

			topLeft.x = x - r;
			topLeft.y = y - r;
			bottomRight.x = x + r;
			bottomRight.y = y + r;

			roiList.add(new Rect(topLeft, bottomRight));
		}

		return roiList;
	}

	private double[] convertMatToArray(Mat mat) {
		double[] data = new double[0];
		try {
			int size = (int) mat.total() * mat.channels();
			data = new double[size];
			mat.get(0, 0, data);
		} catch (Exception e) {
//			LOGGER.warn("No balls were detected.");
		}

		return data;
	}

	public Line findStickLine() throws MissingCueLineException, DetectorException {
		Mat substractedImg = getEdges(getSourceImg().clone());

		List <Line> linesList = getInnerLines(substractedImg);

		Line shortCueLine = cueService.findStickLine(linesList);

		Line longCueLine = null;
		Ball whiteBall = ballService.getWhiteBall();

		if (shortCueLine == null) {
			shortCueLine = cueService.getPreviousAverageLine();
		}

		if (whiteBall != null) {
            Point coordinates = whiteBall.getCenter();
            if (shortCueLine != null) {
				longCueLine = cueService.directAndExtend(shortCueLine, coordinates);
				longCueLine = cueService.stabilizeWithPrevious(longCueLine);
			}
		}

		if (configurableProperties.isDebugActive()) {
			this.debugDetectedLines = linesList;
			this.debugAverageLine = shortCueLine;
		}

		return longCueLine;
	}

	public List<Line> getInnerLines(Mat substractedImage) {
		Line tempLine;

		Mat linesData = new Mat();

		Imgproc.HoughLinesP(substractedImage, linesData, 1, Math.PI/180, TRESHOLD_FOR_INNER_LINES, MIN_LENGTH_FOR_INNER_LINES, MAX_LINE_GAP_FOR_INNER_LINES);

		List <Line> linesList = new ArrayList<>();

		for (int x = 0; x < linesData.rows(); x++){
			double line[] = linesData.get(x, 0);

			tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
			if (bandsService.isPointInsideBand(tempLine.getBegin()) || bandsService.isPointInsideBand(tempLine.getEnd())){
				linesList.add(tempLine);
			}
		}
		return linesList;
	}


	public Mat getEdges(Mat source) throws DetectorException {
		List <Mat> layers = new ArrayList<>();
		Mat dst = new Mat();

		try {
			Imgproc.blur(source, source, new Size(3,3));
			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY);

			Imgproc.threshold(source, source, TRESHOLD_FOR_SOURCE_IMG, 255, Imgproc.THRESH_BINARY);
			Imgproc.Canny(source, dst, TRESHOLD_FOR_CANNY_FIRST, TRESHOLD_FOR_CANNY_SECOND, 3, false);

			Core.subtract(dst, emptyTableImage, dst);

		} catch (Exception e){
			throw new LinesDetectorException("Could not read source stream.", e);
		}

		return dst;
	}

	/**
	 * Get predictions after bump
	 *
	 * @param cueLine cue line
	 *
	 * @return list of predictions
	 *
	 * @throws CueServiceException  if can not predict trajectory after bump
	 * @throws LineServiceException if can not predict trajectory after bump
	 */
	public List<Line> getPredictions(Line cueLine) throws CueServiceException, LineServiceException {
		List <Line> predictions = new ArrayList<>();

		if (cueLine != null) {
			predictions.add(cueLine);
			for (int i = 0; i < properties.getPredictionDepth(); i++){
				Line pred = cueService.predictTrajectoryAfterBump(predictions.get(i));
                predictions.add(pred);
                if (bandsService.getPocketForPoint(pred.getBegin()) != BandsService.PocketPosition.NONE || bandsService.getPocketForPoint(pred.getEnd()) != BandsService.PocketPosition.NONE) {
					break;
				}
			}
		}

		return predictions;
	}

    /**
     * Create target line based on ball collision and aiming line
     *
     * @param line  aiming line
     * @param balls list of balls
     * @param isCue do not return cue ball if it is a cue line
     *
     * @return target line
     *
     * @throws LineServiceException if can not find ball collision line
     */
    public Line createTargetLine(Line line, List<Ball> balls, boolean isCue) throws LineServiceException {
        Ball collision = getCollisionBall(line, balls, isCue);

        if (null != collision) {
            return cueService.findBallCollisionLine(line, collision);
        }

        return null;
    }

    /**
     * Get ball which is in collision with line
     *
     * @param line aiming line
     * @param balls list of balls
     * @param isCueLine do not return cue ball if it is a cue line
     *
     * @return single ball
     */
	public Ball getCollisionBall(Line line, List<Ball> balls, boolean isCueLine) {
		// TODO Usunąć linijke poniżej jeżeli bile będą sortowane w createListOfBalls
		Collections.sort(balls);

		List<Ball> ballsInCollision = new ArrayList<>();
		Map<Double, Integer> distances = new HashMap<>();
		Ball cueBall = balls.get(0);
		double minDistance = 100;
		boolean aboveLine = false;
		double perpendicularCoordinateA = 0;
		double perpendicularCoordinateB = 0;

		if (0 != cueBall.getId()) {
			cueBall = null;
		} else if (isCueLine) {
			// Calculate line perpendicular to cue line
			perpendicularCoordinateA = LineService.calcPerpendicularCoordinate(line);
			perpendicularCoordinateB = -perpendicularCoordinateA * cueBall.getX() + cueBall.getY();
			aboveLine = LineService.isPointAboveTheLine(perpendicularCoordinateA, perpendicularCoordinateB, line.getEnd());

			// Debug
			this.debugPerpendicular = new Line(
					cueBall.getCenter(),
					new Point(
							cueBall.getX() + 100,
							(cueBall.getX() + 100) * perpendicularCoordinateA +  perpendicularCoordinateB
					)
			);
			this.debugLineEndPoint = line.getEnd();
	}

		for (Ball ball : balls) {
			// Ignore cue ball if it is cue line
			if (ball == cueBall && isCueLine) {
				continue;
			}

			double distance = cueService.calculateDistanceBetweenPointAndLine(ball.getCenter(), line);

			if (distance <= properties.getBallExpectedRadius() * 2) {
				// Discard balls behind the cue ball
				if (LineService.isPointAboveTheLine(perpendicularCoordinateA, perpendicularCoordinateB, ball.getCenter()) != aboveLine &&
						isCueLine) {
					continue;
				}

				double distanceBetweenPoints;
				ballsInCollision.add(ball);

				if (isCueLine && null != cueBall) {
					// Calculate distance between object ball and cue ball
					distanceBetweenPoints = LineService.calculateDistanceBetweenPoints(ball.getCenter(), cueBall.getCenter());
				} else {
					// Calculate distance between object ball and bump point
					distanceBetweenPoints = LineService.calculateDistanceBetweenPoints(ball.getCenter(), line.getBegin());
				}

				// Min distance
				if (0 == ballsInCollision.indexOf(ball) || distanceBetweenPoints < minDistance) {
					minDistance = distanceBetweenPoints;
				}

				// Assign distance to ball index
				distances.put(distanceBetweenPoints, ballsInCollision.indexOf(ball));
			}
		}

		if (!ballsInCollision.isEmpty()) {
			// Return closest ball
			return ballsInCollision.get(
					distances.get(minDistance)
			);
		}

		return null;
	}

    /**
     * Return line parallel to cue line started at center of cue ball
     *
     * @param line cue line
     * @param ball cue ball (white ball)
     *
     * @return line started at center of cue ball
     *
     * @throws LineServiceException if can not extend cue line for one side
     */
	public Line refactorCueLine(Line line, Ball ball) throws LineServiceException {
		//line = new Line(new Point(62,84), new Point(456,347));
		//ball = new Ball(20,54,20);
		double distance = cueService.calculateDistanceBetweenPointAndLine(new Point(ball.getX(), ball.getY()), line);
		double[] coordinates = cueService.calcAllCoordinate(line);
		double[] newCoordinates = {coordinates[0], coordinates[1], coordinates[2] + distance};
		double A = newCoordinates[0];
		double B = newCoordinates[1];
		double newC = -ball.getY() - (A / B * ball.getX());

		return lineService.getExtendedStickLineForOneSide(
				new Line(
						new Point(
								ball.getX(),
								ball.getY()
						),
						new Point(
								line.getEnd().x,
								(newC - (A * (line.getEnd().x))) / B
						)
				)
		);
	}

    private Mat prepereEmptyTableForSubs(Mat emptyTableImage) throws DetectorException {
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.cvtColor(emptyTableImage, emptyTableImage, Imgproc.COLOR_BGR2GRAY);
			Imgproc.threshold(emptyTableImage, emptyTableImage, 120, 255, Imgproc.THRESH_BINARY);

		} catch (Exception e){
			throw new DetectorException("Could not prepere empty table image for substract.", e);
		}

		return emptyTableImage;
	}

	public Point getGhostBall(Ball ball, Point pocket) throws LineServiceException {
		Line targetLine = new Line(
				pocket,
				ball.getCenter()
		);
		LineService.switchPoints(targetLine);

		return cueService.getGhostBall(targetLine, ball);
	}

	public List<Line> getDebugDetectedLines() {
		return debugDetectedLines;
	}

	public Line getDebugAverageLine() {
		return debugAverageLine;
	}

	public Point getPointCloserToWhiteBall() {
		return cueService.debugCloserToWhite;
	}

	public Point getPointFurtherToWhiteBall() {
		return cueService.debugFurtherToWhite;
	}

}