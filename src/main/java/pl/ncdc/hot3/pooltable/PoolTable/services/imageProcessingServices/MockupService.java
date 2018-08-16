package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.ProjectProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Queue;

@Service
public class MockupService {
    private final static String fileName = "mockupcameraCalibPic";
    private final static String extension = ".jpg";
    private ArrayList<Mat> mockList;
    private int counter = 0;

    public MockupService()
    {
        mockList = new ArrayList<>();
        for (int i = 0; i < 63; i++)
        {
            try {
                Mat img = Imgcodecs.imread( pl.ncdc.hot3.pooltable.PoolTable.model.Properties.TEST_PATH + fileName + i + ".jpg");

                if (img.empty() || img == null)
                    break;
                mockList.add(img);
            } catch (Exception ex)
            {
                System.out.println("Blad w MockupService");
            }
        }
    }

    public Mat getLiveSnapshot() {
        if (counter == mockList.size())
            counter = 0;
        return mockList.get(counter++);
    }
}
