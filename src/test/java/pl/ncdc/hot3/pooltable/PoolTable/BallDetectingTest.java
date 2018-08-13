package pl.ncdc.hot3.pooltable.PoolTable;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.Drawer;


public class BallDetectingTest {

	private Drawer drawer;
	private Detector detector;
	private Mat img;
	
	@Before
	public void load() {
		 System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		 
		drawer = new Drawer();
		detector = new Detector();
		

	        File input = new File("C:\\Users\\Nats\\Pictures\\pool2Undisorted.png");
	        BufferedImage image;
			try {
				image = ImageIO.read(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
		
			}         
	     	byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();            
	     	img = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
	     	img.put(0, 0, data);
	}
	
	private void printMat(Mat circles) {
		System.out.println("testPrint");
		for (int i = 0; i < circles.cols(); i++) {
			double[] data = circles.get(0, i);
			System.out.println("id: " + i + " x: " + data[0] + " y: " + data[1] + " r: " + data[2]);
		}
		return;
	}

	@Test
	public void drawBallsShouldJustBeDrawingFuckingBalls () {
		Mat result = drawer.drawBalls(img.clone());
		System.out.println("Draw balls test");
     	Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\output.jpg", result);
	}
	
	@Test
	public void detectBallsShouldDetectOnlyBalls() {
		Mat detectedBalls = detector.detectBalls(img.clone());
		System.out.println("detect balls");
		printMat(detectedBalls);
		
	}
	
	@Test
	public void getBallsROI() {
		//List<Ball> balls= detector.createListOfBalls(img);
		Mat detectedBalls = detector.detectBalls(img.clone());
		List<Rect> roi =detector.getBallsROI(detector.convertMatToArray(detectedBalls),img.clone());
		for(Rect roii : roi){
		Imgproc.rectangle(img, new Point(roii.x,roii.y), new Point(roii.x+roii.width,roii.y+roii.height), new Scalar(0, 255, 0));
		}
		Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\roi.jpg", img);
	}
	
	@Test
	public void getBallsId() {
		Mat circles = detector.detectBalls(img.clone());
		List<Rect> roiList = detector.getBallsROI(detector.convertMatToArray(circles), img.clone());
		List<Mat> ballImgList = detector.cropImage(roiList, img);

		// compute mean value of color
		double[] mean = detector.getMeanColor(ballImgList);
		for(int i=0;i<mean.length;i++) {
			
			System.out.println(i +": " + mean[i]);
		}
	}

}
