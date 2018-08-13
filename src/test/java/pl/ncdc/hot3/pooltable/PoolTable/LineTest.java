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
                new Point(1372, 897),
                new Point(1331, 957)
        );
        Line line2 = new Line(
                new Point(1579, 581),
                new Point(1320, 951)
        );

        Line ext1 = detector.getExtendedStickLine(line1);
        Line ext2 = detector.getExtendedStickLine(line2);
        Line asd = Line.getDirectedLine(ext1, ext2);
        System.out.print(asd.getEnd().x);
        System.out.print(asd.getEnd().y);

        Imgproc.line(sourceImage, ext1.getBegin(), ext1.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, ext2.getBegin(), ext2.getEnd(), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        Imgproc.line(sourceImage, asd.getBegin(), asd.getEnd(), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);
        Imgproc.circle(sourceImage, asd.getEnd(), 50, new Scalar(0, 255, 255), 3);
        Imgcodecs.imwrite(BASE_PATH + "line.png", sourceImage);
    }
}
