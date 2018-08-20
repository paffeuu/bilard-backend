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

	private Mat emptyTableImage;
	private Mat sourceImg;
	private Mat outputImg;

	private static Properties properties;
	private CueService cueService;
	private LineService lineService;
	private BallService ballService;

	@Autowired
	public Detector(
			CueService cueService,
			Properties properties,
			BallService ballService,
			LineService lineService
	) {
		this.ballService = ballService;
		this.properties = properties;
		this.cueService = cueService;
		this.lineService = lineService;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		try {
			sourceImg = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), Imgcodecs.IMREAD_COLOR);

			emptyTableImage = Imgcodecs.imread(properties.getFullPath("emptyTable.png"), Imgcodecs.IMREAD_COLOR);
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

	public Mat getOutputImg() {
		return this.outputImg;
	}

	public void setOutputImg(Mat outputImg) {
		this.outputImg = outputImg;
	}

	public ArrayList<Ball> createListOfBalls() throws BallsDetectorException {
		try {
			Mat circles = ballService.detectBalls(sourceImg);

			List<Rect> roiList = ballService.getBallsROI(ballService.convertMatToArray(circles));
			List<Mat> ballImgList = ballService.cropImage(roiList, sourceImg);

			return ballService.createListOfBalls(circles, sourceImg, ballImgList, roiList);
		} catch (Exception e) {
			throw new BallsDetectorException("Unknown exception for create list of balls.", e);
		}
	}

	public Line findStickLine() throws MissingCueLineException, DetectorException {
		Mat substractedImg = getEdges(getSourceImg().clone());
		List <Line> linesList = getInnerLines(substractedImg);
		Line cueLine = cueService.findStickLine(linesList);
		cueLine = cueService.stabilize(cueLine);

		return cueLine;
	}

	private boolean isFrameToTestSave(){
		if (forTestsCounter++ % 4 == 0){
			if (forTestsCounter > 40 && forTestsCounter < 120){
				return true;
			}
		}
		return false;
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

		if (false && isFrameToTestSave() && linesList.size() > 0){
			Mat tests = sourceImg.clone();
			System.out.println("==================================================");
			System.out.println("Lines: " + linesList.size());
			System.out.println("==================================================");
			for (Line line : linesList){
				Imgproc.line(tests, line.getBegin(), line.getEnd(), new Scalar(0, 0, 255), 7);
			}
			Imgcodecs.imwrite("tests/all_lines" + forTestsCounter + ".png", tests);
		}

		return linesList;
	}

	private static int forTestsCounter = 0;
	public Mat getEdges(Mat source) throws DetectorException {
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.blur(source, source, new Size(6,6));

			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			Core.split(source, layers);
			Imgproc.Canny(layers.get(2), source, 50, 200, 3, false);
			Imgproc.threshold(source, source, 200, 255, Imgproc.THRESH_BINARY);

			Core.subtract(source, emptyTableImage, source);

		} catch (Exception e){
			throw new LinesDetectorException("Could not read source stream.", e);
		}

		return source;
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
        Ball collision = cueService.stopLineAtFirstBall(line, balls, isCue);

        if (null != collision) {
            return cueService.findBallColisionLine(line, collision);
        }

        return null;
    }

    private Mat prepereEmptyTableForSubs(Mat emptyTableImage) throws DetectorException {
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.cvtColor(emptyTableImage, emptyTableImage, Imgproc.COLOR_BGR2HSV);
			Core.split(emptyTableImage, layers);

			Imgproc.Canny(layers.get(2), emptyTableImage, 50, 200, 3, false);

		} catch (Exception e){
			throw new DetectorException("Could not prepere empty table image for substract.", e);
		}

		return emptyTableImage;
	}
}


