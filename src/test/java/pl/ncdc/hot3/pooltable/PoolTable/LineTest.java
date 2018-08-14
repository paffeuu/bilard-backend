package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CueServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LineServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.CueService;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.LineService;

public class LineTest {
    String BASE_PATH = "src/main/resources/";

    private LineService lineService = new LineService(new Properties(), new Detector());

    private CueService cueService = new CueService(new Properties(), new Detector(), new LineService());

    @Test
    public void directedLine() throws CueServiceException, LineServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String sourceImagePath = BASE_PATH + "emptyTable.png";
        Mat sourceImage = Imgcodecs.imread(sourceImagePath, Imgcodecs.IMREAD_COLOR);

        Line line1 = new Line(
                new Point(1331, 957),
                new Point(1372, 897)
        );
        Line line2 = new Line(
                new Point(1320, 951),
                new Point(1579, 581)
        );

        Imgproc.circle(sourceImage, line1.getEnd(), 50, new Scalar(0, 255, 255), 3);
        Imgproc.circle(sourceImage, line2.getEnd(), 50, new Scalar(0, 255, 255), 3);


        Line asd = lineService.getDirectedLine(line1, line2);
//        System.out.print(asd.getEnd().x);
//        System.out.print(asd.getEnd().y);

        Point bumpPoint = asd.getEnd();
        Line prediction = cueService.predictTrajectoryAfterBump(asd);
        Line prediction2 = cueService.predictTrajectoryAfterBump(prediction);
        Line prediction3 = cueService.predictTrajectoryAfterBump(prediction2);
        Line prediction4 = cueService.predictTrajectoryAfterBump(prediction3);
        Line prediction5 = cueService.predictTrajectoryAfterBump(prediction4);
        Line prediction6 = cueService.predictTrajectoryAfterBump(prediction5);
        Line prediction7 = cueService.predictTrajectoryAfterBump(prediction6);

        Imgproc.line(sourceImage, line1.getBegin(), line1.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, line2.getBegin(), line2.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.line(sourceImage, asd.getEnd(), new Point(asd.getBegin().x, asd.getEnd().y), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction.getBegin(), prediction.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction2.getBegin(), prediction2.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction3.getBegin(), prediction3.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction4.getBegin(), prediction4.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction5.getBegin(), prediction5.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction6.getBegin(), prediction6.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction7.getBegin(), prediction7.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, prediction.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, prediction2.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, new Point(prediction2.getBegin().x, prediction2.getEnd().y), 50, new Scalar(0, 255, 255), 3);
        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
    }
}
