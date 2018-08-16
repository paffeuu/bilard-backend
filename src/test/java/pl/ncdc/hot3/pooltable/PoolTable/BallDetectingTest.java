//package pl.ncdc.hot3.pooltable.PoolTable;
//
//import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferByte;
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
//import javax.imageio.ImageIO;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.opencv.core.*;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
//import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
//import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DrawerException;
//import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
//import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
//import pl.ncdc.hot3.pooltable.PoolTable.services.*;
//import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {Detector.class, CueService.class,
//        BallService.class, LineService.class, Properties.class})
//public class BallDetectingTest {
//
//    private Drawer drawer;
//    private Mat img;
//
//    @Autowired
//    private Detector detector;
//
//    @Before
//    public void load() throws IOException, DetectorException {
//        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//
//        File input = new File("C:\\Users\\Borat\\Pictures\\New\\2.png");
//        BufferedImage image = ImageIO.read(input);
//        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        img = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
//        img.put(0, 0, data);
//        img = new ImageUndistorterService().undistort(img);
//        detector.setSourceImg(img);
//    }
//
//    @Test
//    public void printListOfBalls() throws BallsDetectorException, DrawerException {
//        //Scalar yellowLowerMask = new Scalar();
//       // Scalar blueLowerMask = new Scalar(100, 100, 50);
//		/*Scalar greenLowerMask = new Scalar();
//		Scalar redLowerMask = new Scalar();
//		Scalar purpleLowerMask = new Scalar();
//		Scalar brownLowerMask = new Scalar();
//		Scalar orangeLowerMask = new Scalar();
//		Scalar yellowHigherMask = new Scalar();*/
//        //Scalar blueHigherMask = new Scalar(115, 255, 255);
//		/*Scalar greenHigherMask = new Scalar();
//		Scalar redHigherMask = new Scalar();
//		Scalar purpleHigherMask = new Scalar();
//		Scalar brownHigherMask = new Scalar();
//		Scalar orangeHigherMask = new Scalar();*/
//		ArrayList<Ball> allBalls;
//		allBalls = detector.createListOfBalls();
//        Collections.sort(allBalls);
//        for(Ball ball: allBalls) {
//            System.out.print("\tx: " + Math.round(ball.getX()) + "  \ty: " + Math.round(ball.getY())
//                    + "  \tr: " + Math.round(ball.getRadius()) + "  \tWhitePixelsPercentage: "
//                    + Math.round((ball.getWhitePixels()/1764)*100) + "%");
//            if((ball.getWhitePixels()*100)/1764 >= 16) {
//                System.out.println(" \t STRIPED");
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 3, new Scalar(0, 255, 0), 3);
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 21, new Scalar(0, 0, 255), 5);
//            } else {
//                System.out.println(" \t SOLID");
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 3, new Scalar(0, 255, 0), 3);
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 21, new Scalar(255, 0, 0), 5);
//            }
//        }
//        Imgcodecs.imwrite("C:\\Users\\Borat\\Pictures\\New\\draw2.png", img);
//
//    }
//}
