package imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class SnapshotGetterService {
    private VideoCapture capture;
    private Mat lastSnapshot = new Mat();

    public SnapshotGetterService() {
        this.capture = new VideoCapture();
        capture.open("rtsp://hot:kamerabilardowa@192.168.253.214:554/Streaming/Channels/1?transportmode=unicast&profile=Profile_1");
    }

    public Mat getLiveSnapshot() {
        if (this.capture.isOpened()) {
            this.capture.read(this.lastSnapshot);
            return this.lastSnapshot;
        } else {
            return null;
        }
    }

    public Mat getLastSnapshot() {
        return this.lastSnapshot;
    }

}
