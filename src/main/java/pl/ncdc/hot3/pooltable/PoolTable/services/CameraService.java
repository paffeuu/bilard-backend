package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;

@Service
public class CameraService {

    private ImageUndistorterService undistorterService;

    @Autowired
    public CameraService(
            ImageUndistorterService undistorterService
    ) {
        this.undistorterService = undistorterService;
    }

    public Mat getSnap(){
        return undistorterService.undistort(OpenCVBufforFlushService.getLastFrame());
    }
}
