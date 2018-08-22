package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Queue;

@Service
public class MockupService {
    private ArrayList<Mat> mockList;
    public int counter = 0;

    private ImageUndistorterService imageUndistorterService;

    @Autowired
    public MockupService(
            ImageUndistorterService imageUndistorterService
    ) {
        this.imageUndistorterService = imageUndistorterService;
        mockList = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < 112; i++)
        {
            try {
                Mat img = Imgcodecs.imread( "src\\main\\resources\\mock\\" + j + ".png");
                j++;
                if(j % 14 == 0) j = 0;

                if (img.empty())
                    break;
                mockList.add(imageUndistorterService.undistort(img));
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public Mat getLiveSnapshot() {
        if (counter == mockList.size())
            counter = 0;
        return mockList.get(counter++);
    }
}
