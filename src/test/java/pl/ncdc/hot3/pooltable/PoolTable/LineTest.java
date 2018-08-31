package pl.ncdc.hot3.pooltable.PoolTable;

import nu.pattern.OpenCV;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.*;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.BandsService;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

import java.util.ArrayList;
import java.util.List;

//@ContextConfiguration(classes = {
//        LineService.class,
//        Properties.class,
//        BandsService.class
//})
@RunWith(SpringJUnit4ClassRunner.class)
public class LineTest {
    String BASE_PATH = "src/main/resources/";

    @Before
    public void onInit(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void testForCoordinatesForVerticalLine(){

        Properties properties = new Properties();
        BandsService bandsService = new BandsService(properties);
        LineService lineService = new LineService(properties, bandsService);

        System.out.println("PIONOWA");

        Point begin = new Point(30, 100);
        Point end = new Point(30, 400);
        Line verticalLine1 = new Line(begin, end);

        begin = new Point(35, 100);
        end = new Point(35, 400);
        Line verticalLine2 = new Line(begin, end);

        System.out.println("KRZYWA");

        begin = new Point(0, 10);
        end = new Point(5, 15);
        Line crossLine1 = new Line(begin, end);

        begin = new Point(15, 200);
        end = new Point(25, 400);
        Line crossLine2 = new Line(begin, end);

        System.out.println("POZIOMA");

        begin = new Point(0, 15);
        end = new Point(5, 15);
        Line horizontalLine1 = new Line(begin, end);

        begin = new Point(15, 200);
        end = new Point(25, 200);
        Line horizontalLine2 = new Line(begin, end);

        System.out.println("Vertical and horizontal");
        System.out.println(lineService.getAngleBetweenLines(verticalLine1, verticalLine2));
        System.out.println(lineService.getAngleBetweenLines(verticalLine1, horizontalLine1));
        System.out.println(lineService.getAngleBetweenLines(verticalLine2, horizontalLine2));
        System.out.println(lineService.getAngleBetweenLines(horizontalLine1, horizontalLine2));

        System.out.println("Cross line 1");
        System.out.println(lineService.getAngleBetweenLines(crossLine1, horizontalLine1));
        System.out.println(lineService.getAngleBetweenLines(crossLine1, verticalLine1));
        System.out.println(lineService.getAngleBetweenLines(crossLine1, crossLine2));

        System.out.println("Cross line 2");
        System.out.println(lineService.getAngleBetweenLines(crossLine2, horizontalLine1));
        System.out.println(lineService.getAngleBetweenLines(crossLine2, verticalLine1));
        System.out.println(lineService.getAngleBetweenLines(crossLine2, crossLine1));

        double[] ABCforCrossLine = lineService.calcABCCoordinates(crossLine1);
        System.out.println("A: " + ABCforCrossLine[0] + "B: " + ABCforCrossLine[1] + "C: " + ABCforCrossLine[2]);

        double[] ABCforCrossLine2 = lineService.calcABCCoordinates(crossLine2);
        System.out.println("A: " + ABCforCrossLine2[0] + "B: " + ABCforCrossLine2[1] + "C: " + ABCforCrossLine2[2]);
    }
}
