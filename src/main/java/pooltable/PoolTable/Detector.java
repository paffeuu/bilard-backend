package pooltable.PoolTable;

import java.util.ArrayList;
import java.util.List;

import imageProcessingServices.ImageUndistorterService;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
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

public class Detector {

	private Mat sourceImg;
	private Mat outputImg;

	public Detector() {
		outputImg = new Mat();
		sourceImg = new Mat();
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
		}
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
