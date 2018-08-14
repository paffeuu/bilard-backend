package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;

public class LineTest {
    String BASE_PATH = "src/main/resources/";

    @Test
    public void directedLine() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

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

        Line ext1 = detector.getExtendedStickLine(line1);
        Line ext2 = detector.getExtendedStickLine(line2);
        Line asd = Line.getDirectedLine(ext1, ext2);
        System.out.print(asd.getEnd().x);
        System.out.print(asd.getEnd().y);

        Point bumpPoint = asd.getEnd();
        Line prediction = detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(bumpPoint, asd));
        Line prediction2 = Line.switchPoints(detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(prediction.getEnd(), prediction)));
        Line prediction3 = Line.switchPoints(detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(prediction2.getEnd(), prediction2)));
        Line prediction4 = detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(prediction3.getEnd(), prediction3));
        Line prediction5 = Line.switchPoints(detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(prediction4.getEnd(), prediction4)));
        Line prediction6 = Line.switchPoints(detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(prediction5.getEnd(), prediction5)));
//        Line prediction7 = detector.getExtendedStickLine(Line.predictTrajectoryAfterBump(prediction6.getEnd(), prediction6));

        Imgproc.line(sourceImage, line1.getBegin(), line1.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, line2.getBegin(), line2.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//        Imgproc.line(sourceImage, asd.getEnd(), new Point(asd.getBegin().x, asd.getEnd().y), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction.getBegin(), prediction.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction2.getBegin(), prediction2.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction3.getBegin(), prediction3.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction4.getBegin(), prediction4.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction5.getBegin(), prediction5.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, prediction6.getBegin(), prediction6.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//        Imgproc.line(sourceImage, prediction7.getBegin(), prediction7.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
//        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, prediction.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, prediction2.getEnd(), 50, new Scalar(0, 255, 255), 3);
//        Imgproc.circle(sourceImage, new Point(prediction2.getBegin().x, prediction2.getEnd().y), 50, new Scalar(0, 255, 255), 3);
        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
    }
}
