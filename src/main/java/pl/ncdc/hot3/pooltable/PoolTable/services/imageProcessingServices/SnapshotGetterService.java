package pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;


@Service
public class SnapshotGetterService {
    private VideoCapture capture;
    private Mat lastSnapshot;

    public SnapshotGetterService() {
        this.capture = new VideoCapture();
        this.lastSnapshot = new Mat();
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
    public VideoCapture getCapture() {
        return this.capture;
    }

    public void setCapture(VideoCapture caputre) {
        this.capture = caputre;
    }
    public void setLastSnapshot(Mat snap) {
        this.lastSnapshot = snap;
    }

}
