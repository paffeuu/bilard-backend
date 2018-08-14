package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.services.Detector;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;

public class UndistordTest {

    String BASE_PATH = "src/main/resources/";

    @Test
    public void shouldSaveUndistortedImages(){

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String fileUrl = BASE_PATH + "jacek" ;

        ImageUndistorterService undistorterService = new ImageUndistorterService();

        for (int i = 0; i < 10; i++){
            String file = fileUrl + (i+1) + ".png";
            String fileTemp = fileUrl + (i+1) + ".png";

            Mat sourceWithStickImg = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);
            Mat dst = undistorterService.undistort(sourceWithStickImg);

            Imgcodecs.imwrite(fileTemp, dst);
        }
    }

}
