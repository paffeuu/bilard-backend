package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

@Component
public class OpenCVBufforFlushService {
    private static VideoCapture capture = new VideoCapture();
    private static Vector<Mat> frameVector = new Vector<Mat>();
    private static boolean isNotNeeded = true;

    @Scheduled(fixedRate = 250)
    public static void getFrame() {
        if (isNotNeeded) {
            if (!capture.isOpened()) {
                System.out.println("open");
                capture.open("rtsp://hot:kamerabilardowa@192.168.253.214:554/Streaming/Channels/1?transportmode=unicast&profile=Profile_1");
            }
            System.out.println("czyta");
            Mat newFrame = new Mat();
            try {
                capture.read(newFrame);
                if ( frameVector.size() >= 26) {
                    for ( int i =0; i< 24; i++) {
                        frameVector.remove(0);
                    }
                }
                frameVector.add(newFrame);
                //Imgcodecs.imwrite("test.jpg", lastFrame);
                System.out.println("przeczytal");
                //OpenCVBufforFlushService.setIsNotNeeded(false);
            } catch ( Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static Mat getLastFrame() {
        return frameVector.get(frameVector.size()-2);
    }
    public static void setIsNotNeeded(boolean b) {
        isNotNeeded = b;
    }
}
