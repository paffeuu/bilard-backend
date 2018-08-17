package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.ProjectProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;

@Service
public class MockupService {
    private final static String fileName = "mockupcameraCalibPic";
    private final static String extension = ".jpg";
    private ArrayList<Mat> mockList;
    public int counter = 0;

    public MockupService()
    {
        mockList = new ArrayList<>();
        for (int i = 0; i < 100; i++)
        {
            try {
                Mat img = Imgcodecs.imread("C:\\Users\\Arcimboldo\\Desktop\\CamerCalib\\mockup\\" + fileName + String.valueOf(i) + ".jpg");
                if (img.empty())
                    break;
                mockList.add(img);
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
