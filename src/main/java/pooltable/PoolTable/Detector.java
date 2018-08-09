package pooltable.PoolTable;

import java.util.ArrayList;
import java.util.List;

import imageProcessingServices.ImageUndistorterService;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pooltable.exceptions.DetectorException;
import org.opencv.photo.Photo;
import pooltable.PoolTable.model.Ball;

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

	static final Logger LOGGER = LoggerFactory.getLogger(Detector.class);

	private Mat sourceImg;
	private Mat outputImg;
	private Mat cannyImg;

	public Detector() {
		try {
			sourceImg = Imgcodecs.imread(ProjectProperties.EMPTY_TABLE_IMG, Imgcodecs.IMREAD_COLOR);
			cannyImg = getEdges(sourceImg);
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

		//undisortion
		ImageUndistorterService source = new ImageUndistorterService();
		sourceImg = source.undistort(sourceImg);

		// blur image
		Imgproc.blur(sourceImg, outputImg, new Size(1, 1));

		// convert to hsv
		Imgproc.cvtColor(outputImg, outputImg, Imgproc.COLOR_BGR2HSV);

		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(outputImg, planes);

		// canny - detect edges
		Mat edges = new Mat();
		int highThreshold = 105;
		int ratio = 3;
		Imgproc.Canny(planes.get(2), edges, highThreshold/ratio, highThreshold);

		// detect circles
		Mat circles = new Mat(); // contains balls coordinates
		int maxRadius = 22;
		int minRadius = 16;
		int minDistance = 36;
		Imgproc.HoughCircles(edges, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDistance,
				120, 10, minRadius, maxRadius);

		System.out.println(circles.dump());

		return circles;
	}

	public void drawBalls() {

		// get balls coordinates
		Mat detectedBalls = detectBalls();

		int x,y,r;
		int j = 0;
		int leftBand = 175;
		int rightBand = sourceImg.width() - 105;
		int topBand = 350;
		int bottomBand = sourceImg.height() - 300;


		for (int i = 0; i < detectedBalls.cols(); i++) {
			// read ball coordinates
			double[] data = detectedBalls.get(0, i);

				x = (int) data[0];
				y = (int) data[1];
				r = (int) data[2];
			if((x > leftBand && x < rightBand) && (y > topBand && y < bottomBand)) {
				j++;
				System.out.println("id: "+ j +" x: " + data[0] + " y: " + data[1] + " radius: " + r);
				Point center = new Point(x, y);

				// draw circle center
				Imgproc.circle(sourceImg, center, 3, new Scalar(0, 255, 0), -1);

				// draw circle outline
				int radius = 20;
				Imgproc.circle(sourceImg, center, radius, new Scalar(0, 0, 255), 1);
			}
			
			Point center = new Point(x, y);

			// draw circle center
			Imgproc.circle(outputImg, center, 3, new Scalar(0, 255, 0), -1);
			
			// draw circle outline
			int radius = 10;
			Imgproc.circle(outputImg, center, radius, new Scalar(0, 0, 255), 1);
		}

	}

	private Mat getEdges(Mat source) throws DetectorException {
		Mat dst = new Mat(), cdst = new Mat(), cdstP;
		List <Mat> layers = new ArrayList<>();

		try {
			Imgproc.blur(source, source, new Size(4,4));

			Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
			Core.split(source, layers);
			Imgproc.Canny(layers.get(1), dst, 50, 200, 3, false);

		} catch (NullPointerException e){
			throw new DetectorException("Could not read source stream.", e);
		}

		return dst;
	}

	public Line findStickLine(Mat source) throws DetectorException {

		Line tempLine = null;

		Mat substractedImg = new Mat();
		Mat linesP = getEdges(source);

		Core.subtract(linesP, cannyImg, substractedImg);

		Imgproc.HoughLinesP(substractedImg, linesP, 1, Math.PI/180, 50, 50, 10);

		for (int x = 0; x < linesP.rows(); x++){
			double line[] = linesP.get(x, 0);

			tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
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

	public ArrayList<Ball> createListOfBalls() {
		int x,y,r;
		Mat circles = detectBalls();
		ArrayList<Ball> balls = new ArrayList<>();

		for (int i = 1; i <= circles.cols(); i++) {
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
}
