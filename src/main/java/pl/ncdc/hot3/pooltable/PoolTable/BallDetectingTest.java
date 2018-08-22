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
//import org.opencv.core.*;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import pl.ncdc.hot3.pooltable.PoolTable.exceptions.BallsDetectorException;
//import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
//import pl.ncdc.hot3.pooltable.PoolTable.services.BallService;
//import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
//import pl.ncdc.hot3.pooltable.PoolTable.services.Drawer;
//import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
//
//public class BallDetectingTest {
//
//    private Drawer drawer;
//    private Detector detector;
//    private Mat img;
//    private Mat img13;
//    private BallService ballService;
//
//    @Before
//    public void load() throws IOException {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        File input = new File("C:\\Users\\Borat\\Pictures\\New\\1.png");
//        BufferedImage image = ImageIO.read(input);
//        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        img = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
//        img.put(0, 0, data);
//        img = new ImageUndistorterService().undistort(img);
//        //Imgcodecs.imwrite("C:\\Users\\Borat\\Pictures\\New\\un1.png", img);
//    }
//
//    @Test
//    public void detectBallsAndROI() throws BallsDetectorException {
//
//// only for solid
//        Scalar blackLowerMask = new Scalar(0, 0, 0);
//        Scalar blackHigherMask = new Scalar(180, 255, 35);
//
//        Scalar purpleLowerMask = new Scalar(115, 100, 50);
//        Scalar purpleHigherMask = new Scalar(140, 255, 150);
//
//        Scalar brownLowerMask = new Scalar(170, 0, 0);
//        Scalar brownHigherMask = new Scalar(180, 255, 120);
//
//        Scalar orangeLowerMask = new Scalar(2,100,100);
//        Scalar orangeHigherMask = new Scalar(15,255,250);
//
//        Scalar red1LowerMask = new Scalar(0, 180, 50);
//        Scalar red1HigherMask = new Scalar(3, 255, 200);
//        Scalar red2LowerMask = new Scalar(170, 180, 130);
//        Scalar red2HigherMask = new Scalar(180, 255, 200);
//
//        Scalar blueLowerMask = new Scalar(100, 120, 95);
//        Scalar blueHigherMask = new Scalar(115, 255, 180);
//
////works for solid and stripe
//        Scalar yellowLowerMask = new Scalar(20, 50, 50);
//        Scalar yellowHigherMask = new Scalar(40, 255, 255);
//
//        Scalar greenLowerMask = new Scalar(85,100,0);
//        Scalar greenHigherMask = new Scalar(100,255,80);
//
//// detect crop balls
//        Mat mat;
//        Mat mat2 = new Mat();
//
//        List<Mat> matList = new ArrayList<>();
//
//        Mat circles = ballService.detectBalls(img.clone());
//        List<Rect> roiList = ballService.getBallsROI(ballService.convertMatToArray(circles));
//        List<Mat> ballImgList = ballService.cropImage(roiList, img.clone());
//        ArrayList<Ball> balls = ballService.createListOfBalls(circles, img.clone(), ballImgList, roiList);
//
//// masks
//        Imgproc.cvtColor(img, mat2, Imgproc.COLOR_BGR2HSV);
//        ArrayList<Double> mean = detector.getMeanColor(ballImgList);
//
//// color masks
//        for (int l = 0; l < balls.size(); l++) {
//
//            Imgproc.cvtColor(ballImgList.get(l), ballImgList.get(l), Imgproc.COLOR_BGR2HSV);
//
//            Mat blue = new Mat();
//            Mat orig = new Mat();
//            Mat red = new Mat();
//            Mat red2 = new Mat();
//            Core.inRange(ballImgList.get(l), greenLowerMask, greenHigherMask, blue);
////Core.inRange(ballImgList.get(l), red2LowerMask, red2HigherMask, red2);
////Core.addWeighted(blue, 1.0, red2, 1.0, 0.0, red);
//            Imgproc.cvtColor(ballImgList.get(l), orig, Imgproc.COLOR_HSV2BGR);
//            //Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Mask\\crop" + l + ".png", orig);
//            //Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Mask\\mask" + l + ".png", blue);
////Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Mask\\red1_" + l + ".png", blue);
////Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Mask\\red2_" + l + ".png", red2);
//        }
//
//        mat = img.clone();
//        Core.split(mat, matList);
//
//        Imgproc.equalizeHist(matList.get(0), matList.get(0));
//        Imgproc.equalizeHist(matList.get(1), matList.get(1));
//        Imgproc.threshold(matList.get(0), matList.get(0), 200, 255, Imgproc.THRESH_BINARY);
//        Imgproc.threshold(matList.get(1), matList.get(1), 200, 255, Imgproc.THRESH_BINARY);
//
//        List<Mat> listOfB = ballService.cropImage(roiList, matList.get(0));
//        List<Mat> listOfG = ballService.cropImage(roiList, matList.get(1));
//        Mat histB = new Mat();
//        Mat histG = new Mat();
//        MatOfFloat ranges = new MatOfFloat(0f, 256f);
//        MatOfInt channels = new MatOfInt(0);
//        MatOfInt histSize = new MatOfInt(2);
//
//// cover white circles
//        Mat crop = new Mat();
//        Mat circles2 = new Mat();
//        Mat edges = new Mat();
//        Point center = new Point();
//        Mat mask = new Mat(crop.width(), crop.height(), 0);
//        double[] data = new double[3];
//
//
//        int minR = 7;
//        int maxR = 10;
//        int dist = maxR*3;
//
///*for (int j = 0; j < listOfB.size(); j++) {
//crop = listOfB.get(j);
//Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Lista\\cropB" + j + ".png", crop);
//
//Imgproc.Canny(crop, edges, 100, 100*3);
//Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Lista\\cropBedges" + j + ".png", edges);
//
//Imgproc.HoughCircles(edges, circles2, Imgproc.CV_HOUGH_GRADIENT, 1.0, dist, 100, 100, minR, maxR);
//
//System.out.println("cir s: "+circles.size()+" size: " + new Size(16,1) + " comp: "+circles.size().equals(new Size(16,1)));
//for(int i=0; i<circles.cols(); i++) {
//if (circles.size().equals(new Size(16,1))) {
//circles2.convertTo(circles2, CvType.CV_64FC3);
//circles2.get(0, i, data);
//printMat(circles2);
//center.x = data[0];
//center.y = data[1];
//int r = (int) data[2];
//Imgproc.circle(crop, center, r, new Scalar(100, 255, 100), 1, 8, 0);
//Imgcodecs.imwrite("C:\\Users\\Nats\\Pictures\\Lista\\cropBnew" + j + ".png", crop);
//}
//}
//}*/
//
//// #mod
//        Mat empty = new Mat();
//        for (int k = 0; k < listOfB.size(); k++) {
//            Imgproc.calcHist(Arrays.asList(listOfB.get(k)), channels, empty, histB, histSize, ranges);
//            Imgproc.calcHist(Arrays.asList(listOfG.get(k)), channels, empty, histG, histSize, ranges);
//            if (histB.get(1, 0)[0] > 3 * histG.get(1, 0)[0]) {
//                balls.get(k).setWhitePixels(histG.get(1, 0)[0]);
//            } else if (histG.get(1, 0)[0] > 3 * histB.get(1, 0)[0]) {
//                balls.get(k).setWhitePixels(histB.get(1, 0)[0]);
//            } else {
//                balls.get(k).setWhitePixels((histB.get(1, 0)[0] + histG.get(1, 0)[0]) / 2);
//            }
//        }
//
//        Collections.sort(balls);
//        for (Ball ball : balls) {
//            System.out.print("\tx: " + Math.round(ball.getX()) + " \tcolorH: " + Math.round(ball.getMedianColorH())
//                    + " \tcolorS: " + Math.round(ball.getMedianColorS()) + " \tcolorV: "
//                    + Math.round(ball.getMedianColorV()) + " \ty: " + Math.round(ball.getY())
//                    + " \tr: " + Math.round(ball.getRadius()) + " \tWhitePixelsPercentage: "
//                    + Math.round((ball.getWhitePixels() / 1764) * 100) + "%");
//            if ((ball.getWhitePixels() * 100) / 1764 >= 16) {
//                System.out.println(" \t STRIPED");
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 3, new Scalar(0, 255, 0), 3);
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 21, new Scalar(0, 0, 255), 5);
//            } else {
//                System.out.println(" \t SOLID");
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 3, new Scalar(0, 255, 0), 3);
//                Imgproc.circle(img, new Point(ball.getX(), ball.getY()), 21, new Scalar(255, 0, 0), 5);
//            }
//        }
//        Imgcodecs.imwrite("C:\\Users\\Borat\\Pictures\\New\\draw1.png", img);
//    }
//}