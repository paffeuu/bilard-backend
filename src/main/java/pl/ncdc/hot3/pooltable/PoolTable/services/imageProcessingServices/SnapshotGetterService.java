package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;


//@Service
public class SnapshotGetterService {
    private VideoCapture capture;
    private Mat lastSnapshot;
    private boolean isOpening = false;

    public SnapshotGetterService() {
        this.capture = new VideoCapture();
        this.lastSnapshot = new Mat();
        capture.open("rtsp://hot:kamerabilardowa@192.168.253.214:554/Streaming/Channels/1?transportmode=unicast&profile=Profile_1");
    }

    public Mat getLiveSnapshot() {
        if (this.capture.isOpened()) {
            System.out.println("open for read");
            Mat result = new Mat();
            try {
                this.capture.read(this.lastSnapshot);
            } catch (Exception e) {
                System.out.println("read unknown error");
                this.reset();
                return null;
            }
            return this.lastSnapshot;
        }
        System.out.println("resetting");
        this.reset();
        return null;
    }

    public boolean grabFrame() {
        return this.capture.grab();
    }

    public Mat getLastSnapshot() {
        return this.lastSnapshot;
    }

    public VideoCapture getCapture() {
        return this.capture;
    }

    public void setCapture(VideoCapture caputre) {
        this.capture = caputre;
    }

    public void setLastSnapshot(Mat snap) {
        this.lastSnapshot = snap;
    }

    public boolean isOpening() {
        return this.isOpening;
    }

    public void reset() {
        this.isOpening = true;
        this.capture = new VideoCapture();
        this.lastSnapshot = new Mat();
        capture.open("rtsp://hot:kamerabilardowa@192.168.253.214:554/Streaming/Channels/1?transportmode=unicast&profile=Profile_1");
        this.isOpening = false;
    }
}
