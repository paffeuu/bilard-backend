package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CameraLoadingException;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.CameraServiceException;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.MockupService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;

@Service
public class CameraService {

    public static final Logger LOGGER = LoggerFactory.getLogger(CameraService.class);

    private ImageUndistorterService undistorterService;

    @Autowired
    public CameraService(
            ImageUndistorterService undistorterService
    ) {
        this.undistorterService = undistorterService;
    }


    public Mat getSnap() throws CameraServiceException {
        Mat out = null;

        try {
            out = undistorterService.undistort(OpenCVBufforFlushService.getLastFrame());
        } catch (NullPointerException e){
            throw new CameraServiceException("Undistorted camera view is not available.");
        }

        if (out == null) {
            throw new CameraServiceException("Image to undistort cannot be null.");
        }

        return out;
    }
}
