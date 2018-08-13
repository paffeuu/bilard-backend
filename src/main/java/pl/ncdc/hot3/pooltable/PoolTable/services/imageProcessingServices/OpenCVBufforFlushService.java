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

    private static int counter = 0;
    private static VideoCapture capture = new VideoCapture();
    private static boolean isNotNeeded = true;

    private static Mat[] framesArray = new Mat[10];

    @Scheduled(fixedRate = 125)
    public static void getFrame() {
        if (isNotNeeded) {

            if (!capture.isOpened()) {
                capture.open("rtsp://hot:kamerabilardowa@192.168.253.214:554/Streaming/Channels/1?transportmode=unicast&profile=Profile_1");
            }
            Mat newFrame = new Mat();
            try {
                capture.read(newFrame);
                if (!newFrame.empty()){
                    counter++;
                    framesArray[counter%10] = newFrame;
                    //System.out.println("przeczytal klatka: "+ counter);
                }else {
                    capture = new VideoCapture();
                    System.gc();
                }
                if (counter % 200 == 0){
                    capture = new VideoCapture();
                    System.gc();
                }
            } catch ( Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Mat getLastFrame() {
        return framesArray[counter%10];
    }

    public static void setIsNotNeeded(boolean b) {
        isNotNeeded = b;
    }
}