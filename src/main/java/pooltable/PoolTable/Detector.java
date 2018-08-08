package pooltable.PoolTable;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

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

	public void setSourseImg(Mat sourceImg) {
		this.sourceImg = sourceImg;
	}

	public Mat getOutputImg() {
		return this.outputImg;
	}

	public void setOutputImg(Mat outputImg) {
		this.outputImg = outputImg;
	}

	public Mat detectBalls() {

		Imgproc.blur(sourceImg, outputImg, new Size(3, 3)); // blur image
		/*Mat img = outputImg;
		Mat color_boost = new Mat();
		Mat grey = new Mat();
		Photo.decolor(img, grey, color_boost);
		outputImg = color_boost;*/
		//Imgproc.cvtColor(img, outputImg, Imgproc.COLOR_RGB2);
		Imgcodecs.imwrite("C:\\Users\\Nats\\Desktop\\color.jpg", outputImg);
		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(outputImg, planes);

		// canny - detect edges
		Mat edges = new Mat();
		int lowThreshold = 40; //TODO
		int ratio = 3; //TODO

		Imgproc.Canny(planes.get(1), edges, lowThreshold, lowThreshold * ratio);
		Imgcodecs.imwrite("C:\\Users\\Nats\\Desktop\\cannyluv.jpg", edges);
		// detect circles
		Mat circles = new Mat(); // contains balls coordinates

		int maxRadius = 22;
		int minRadius = 19;
		int minDistance = minRadius;

		Imgproc.HoughCircles(edges, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, minDistance, 120, 10, minRadius,
				maxRadius);

		System.out.println(circles);

		return circles;
	}

	public void drawBalls() {

		// get balls coordinates
		Mat detectedBalls = new Mat();
		detectedBalls = detectBalls();

		double x = 0.0;
		double y = 0.0;
		int r = 0;

		for (int i = 0; i < detectedBalls.cols(); i++) {
			// read ball coordinates
			double[] data2 = detectedBalls.get(0, i);

				x = data2[0];
				y = data2[1];
				r = (int) data2[2];

				System.out.println("i: "+i+" x: " + data2[0] + " y: " + data2[1] + " radius: " + data2[2]);
			
			Point center = new Point(x, y);

			// draw circle center
			Imgproc.circle(outputImg, center, 3, new Scalar(0, 255, 0), -1);
			
			// draw circle outline
			int radius = 19;
			Imgproc.circle(outputImg, center, radius, new Scalar(0, 0, 255), 1);
		}

	}

}
