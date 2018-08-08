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
import pooltable.exceptions.DetectorException;

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
	private List<Line> staticLines;

	public Detector() {
		staticLines = new ArrayList<>();
	}

	public Mat getSourceImg() {
		return this.sourceImg;
	}

	public void setSourceImg(Mat sourceImg) {
		this.sourceImg = sourceImg;
	}

	public Mat getOutputImg() {
		return this.sourceImg;
	}

	public void setOutputImg(Mat outputImg) {
		this.outputImg = outputImg;
	}

	public List<Line> getStaticLines() {
		return staticLines;
	}

	public void setStaticLines(List<Line> staticLines) {
		this.staticLines = staticLines;
	}

	public Mat detectBalls() {

		Imgproc.blur(outputImg, sourceImg, new Size(3, 3)); // blur image

		// split into planes
		List<Mat> planes = new ArrayList<>(3);
		Core.split(outputImg, planes);

		// canny - detect edges
		Mat edges = new Mat();
		int lowThreshold = 40;
		int ratio = 3;

		Imgproc.Canny(planes.get(1), edges, lowThreshold, lowThreshold * ratio);

		// detect circles
		Mat circles = new Mat(); // contains balls coordinates

		int maxRadius = 10;
		int minRadius = 7;
		int minDistance = maxRadius;

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

			for (int j = 0; j < data2.length; j++) {
				x = data2[0];
				y = data2[1];
				r = (int) data2[2];

				System.out.println("x: " + data2[0] + " y: " + data2[1] + " radius: " + data2[2]);
			}
			Point center = new Point(x, y);

			// draw circle center
			Imgproc.circle(outputImg, center, 3, new Scalar(0, 255, 0), -1);
			
			// draw circle outline
			int radius = 10;
			Imgproc.circle(outputImg, center, radius, new Scalar(0, 0, 255), 1);
		}

	}

	private Mat getLines() throws DetectorException {
		Mat dst = new Mat(), cdst = new Mat(), cdstP;

		try {
			Imgproc.Canny(sourceImg, dst, 50, 200, 3, false);
		} catch (NullPointerException e){
			throw new DetectorException("Could not read source stream.", e);
		}

		Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
		cdstP = cdst.clone();
		Mat linesP = new Mat();
		Imgproc.HoughLinesP(dst, linesP, 1, Math.PI/180, 50, 50, 10);

		return linesP;
	}

	public void saveStaticLines() throws DetectorException {

		Mat linesP = getLines();

		for (int x = 0; x < linesP.rows(); x++){
			double line[] = linesP.get(x, 0);
			staticLines.add(new Line(new Point(line[0], line[1]), new Point(line[2], line[3])));
			//Imgproc.line(cdstP, new Point(line[0], line[1]), new Point(line[2], line[3]), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
		}

		//Imgcodecs.imwrite("OUT.png", cdstP);
	}

	public Line findStickLine() throws DetectorException {

		Mat newLines = new Mat();
		Mat linesP = getLines();

		for (int x = 0; x < linesP.rows(); x++){
			double line[] = linesP.get(x, 0);
			Line tempLine = new Line(new Point(line[0], line[1]), new Point(line[2], line[3]));
			if (!staticLines.contains(tempLine)){
				System.out.println(x + " nowa linia + " + tempLine.toString());
				Imgproc.line(newLines, new Point(line[0], line[1]), new Point(line[2], line[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
				Imgcodecs.imwrite("OUT.png", newLines);

				return tempLine;
			}
		}

		return null;
	}

	public Line getExtendedStickLine(Line stickLine){

		double Y = (stickLine.getBegin().y - stickLine.getEnd().y);
		double X = (stickLine.getBegin().x - stickLine.getEnd().x);

		double a = Y/X;
		double b = Y - (a*X);

		// X = 0
		// X = max



		return new Line();

	}

}
