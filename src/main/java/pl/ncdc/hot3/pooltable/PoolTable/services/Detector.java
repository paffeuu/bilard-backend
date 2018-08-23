package pl.ncdc.hot3.pooltable.PoolTable.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;


@Service
public class Detector {

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat emptyTableImage;
	private Mat sourceImg;
	private Mat outputImg;

	private static Properties properties;
	private CueService cueService;
    private BallService ballService;

	@Autowired
	public Detector(
			CueService cueService,
			Properties properties,
			BallService ballService
	) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.ballService = ballService;
		this.properties = properties;
		this.cueService = cueService;

		try {
			sourceImg = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), CvType.CV_64F);

			emptyTableImage = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), CvType.CV_64F);
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


		return ballService.createListOfBalls(filteredBalls, sourceImg.clone(), roiList);
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
		Imgproc.HoughCircles(planes.get(2), destinationImage, Imgproc.CV_HOUGH_GRADIENT, 1.0, properties.getBallMinDistance(),
				30, 15, properties.getBallMinRadius(), properties.getBallMaxRadius());
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

	private List<Rect> getBallsROI(double[] circles) {
		double x, y, r;
		Point topLeft = new Point();
		Point bottomRight = new Point();

		List<Rect> roiList = new ArrayList<>();

		for (int i = 0; i < circles.length; i += 3) {
			x = circles[i];
			y = circles[i + 1];
			r = 21;

			topLeft.x = x - r;
			topLeft.y = y - r;
			bottomRight.x = x + r;
			bottomRight.y = y + r;

			if(topLeft.x > properties.getTableBandLeft() && topLeft.y > properties.getTableBandTop()) {
				if(bottomRight.x < properties.getTableBandRight() && bottomRight.y < properties.getTableBandBottom()) {
					roiList.add(new Rect(topLeft, bottomRight));
				}
			}
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

        if (shortCueLine != null && ballService.getWhiteBall() != null) {
            Ball whiteBall = ballService.getWhiteBall();
            Point coordinates = new Point(whiteBall.getX(), whiteBall.getY());

            longCueLine = cueService.directAndExtend(shortCueLine, coordinates);
            //longCueLine = cueService.stabilizeWithPrevious(longCueLine);
		}


		return longCueLine;
	}

	public List<Line> getInnerLines(Mat substractedImage) {
		Line tempLine;

		Mat linesData = new Mat();


		Imgproc.HoughLinesP(substractedImage, linesData, 1, Math.PI/180, 70, 50, 10);

		List <Line> linesList = new ArrayList<>();

		for (int x = 0; x < linesData.rows(); x++){
			double line[] = linesData.get(x, 0);

			tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
			if (properties.isPointInsideBand(tempLine.getBegin()) || properties.isPointInsideBand(tempLine.getEnd())){
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

			Imgproc.threshold(source, source, 140, 255, Imgproc.THRESH_BINARY);
			Imgproc.Canny(source, dst, 100, 40, 3, false);


			Core.subtract(dst, emptyTableImage, dst);

		} catch (Exception e){
			throw new LinesDetectorException("Could not read source stream.", e);
		} finally {
			source.release();
		}

		source = dst.clone();
		dst.release();

		return source;
	}

	public List<Line> getPredictions(Line cueLine) throws CueServiceException, LineServiceException {
		List <Line> predictions = new ArrayList<>();

		if (cueLine != null) {
			predictions.add(cueLine);
			for (int i = 0; i < properties.getPredictionDepth(); i++){
				Line pred = cueService.predictTrajectoryAfterBump(predictions.get(i));
				predictions.add(pred);
				if (properties.isPointGoingToSocket(pred.getBegin()) || properties.isPointGoingToSocket(pred.getEnd()))
					break;
			}
		}

		return predictions;
	}

    public Line createTargetLine(Line line, List<Ball> balls, boolean isCue) throws LineServiceException {
        Ball collision = cueService.stopLineAtFirstBall(line, balls, isCue);

        if (null != collision) {
            return cueService.findBallColisionLine(line, collision);
        }

        return null;
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





}


