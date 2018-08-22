package pl.ncdc.hot3.pooltable.PoolTable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CameraServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.*;
import pl.ncdc.hot3.pooltable.PoolTable.services.Settings.PathService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;

import java.io.FileNotFoundException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        TableStoryService.class,
        PreviousPositionService.class,
        Drawer.class,
        Detector.class,
        CueService.class,
        LineService.class,
        Properties.class,
        CameraService.class,
        ImageUndistorterService.class,
        PathService.class
})
public class TableStoryServiceTests {

    private TableStoryService tableStoryService;

    @Autowired
    private Detector detector;

    @Autowired
    private Drawer drawer;

    @Autowired
    private Properties properties;

    @Autowired
    private PreviousPositionService previousPositionService;

    @Autowired
    private PathService pathService;

    @Test
    public void shouldReturnPoolTableModelWithAllDetailsAndSaveNewImage() throws CameraServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat source = Imgcodecs.imread(pathService.TESTS_PATH + "xxxx.png", CvType.CV_64F);


        CameraService cameraService = mock(CameraService.class);
            when(cameraService.getSnap()).thenReturn(source);
        System.out.println("=======================================================");

        System.out.println(source.width());

        tableStoryService = new TableStoryService(detector, cameraService, drawer, properties, previousPositionService);

        PoolTable table = tableStoryService
                .next()
                .findCue()
                .makePredictions()
                .findBalls()
                .showPrevious()
                .build();

        System.out.println("Cue details: " + table.getCue());
        System.out.println("Number of bills: " + table.getBalls().size());


        Mat output = Imgcodecs.imdecode(new MatOfByte(table.getTableImage()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        Imgcodecs.imwrite(pathService.BASE_PATH + "TableStoryService_Test.png", output);

        Assert.assertNotNull(table);
        Assert.assertFalse(table.getBalls().isEmpty());
        Assert.assertNotNull(table.getTableImage());
    }

    @Test
    public void shouldReturnEmptyTableForEmptyTablePhoto() throws CameraServiceException, FileNotFoundException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat source = Imgcodecs.imread(pathService.getFullPath("emptyTable.png"), CvType.CV_64F);
        CameraService cameraService = mock(CameraService.class);
            when(cameraService.getSnap()).thenReturn(source);

        tableStoryService = new TableStoryService(detector, cameraService, drawer, properties, previousPositionService);

        PoolTable table = tableStoryService
                .next()
                .findCue()
                .makePredictions()
                .findBalls()
                .showPrevious()
                .build();

        System.out.println("Cue details: " + table.getCue());
        System.out.println("Number of bills: " + table.getBalls().size());

        Mat output = Imgcodecs.imdecode(new MatOfByte(table.getTableImage()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        Imgcodecs.imwrite(pathService.BASE_PATH + "TableStoryService_empty_Test.png", output);

        Assert.assertNotNull(table);
        Assert.assertTrue(table.getBalls().isEmpty());
        Assert.assertNotNull(table.getTableImage());
    }

    @Test(expected = CameraServiceException.class)
    public void shouldThrowCameraExceptionWhenSourceNotAvailable() throws CameraServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat source = Imgcodecs.imread(pathService.BASE_PATH + "hahahhah.png", CvType.CV_64F);

        CameraService cameraService = mock(CameraService.class);
        when(cameraService.getSnap()).thenReturn(source);

        tableStoryService = new TableStoryService(detector, cameraService, drawer, properties, previousPositionService);

        PoolTable table = tableStoryService
                .next()
                .findCue()
                .makePredictions()
                .findBalls()
                .showPrevious()
                .build();

        Assert.assertNotNull(table);
        Assert.assertFalse(table.getBalls().isEmpty());
        Assert.assertNotNull(table.getTableImage());
    }

    @Test(expected = CameraServiceException.class)
    public void shouldThrowCameraExceptionWhenSourceNull() throws CameraServiceException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        CameraService cameraService = mock(CameraService.class);
            when(cameraService.getSnap()).thenReturn(null);

        tableStoryService = new TableStoryService(detector, cameraService, drawer, properties, previousPositionService);

        PoolTable table = tableStoryService
                .next()
                .findCue()
                .makePredictions()
                .findBalls()
                .showPrevious()
                .build();

        Assert.assertNotNull(table);
        Assert.assertFalse(table.getBalls().isEmpty());
        Assert.assertNotNull(table.getTableImage());
    }

    @Test
    public void shouldMakeTablesForManyImagesAndSaveThemAll() throws CameraServiceException, FileNotFoundException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ImageUndistorterService undistorterService = new ImageUndistorterService();
        CameraService cameraService = mock(CameraService.class);
        tableStoryService = new TableStoryService(detector, cameraService, drawer, properties, previousPositionService);

        PoolTable table = new PoolTable();
        Mat source;

        int size = 1;
        for (int i = 0; i < size; i++){
            String filename = "mock/mockupcameraCalibPic" + i + ".jpg";
            String fullPath = pathService.getFullPath(filename);

            source = Imgcodecs.imread(fullPath, CvType.CV_64F);
                when(cameraService.getSnap()).thenReturn(undistorterService.undistort(source));

            table = tableStoryService
                    .next()
                    .findCue()
                    .makePredictions()
                    .findBalls()
                    .showPrevious()
                    .build();

            Mat output = Imgcodecs.imdecode(new MatOfByte(table.getTableImage()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            String fileName = "tests/fromMock_" + i + ".jpg";
            Imgcodecs.imwrite(fileName, output);
        }

        Assert.assertNotNull(table);
        Assert.assertTrue(table.getBalls().isEmpty());
        Assert.assertNotNull(table.getTableImage());
    }

}
