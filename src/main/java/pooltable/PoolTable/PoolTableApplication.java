package pooltable.PoolTable;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"imageProcessingServices","pooltable"})
public class PoolTableApplication {
    static {
        String opencvpath = System.getProperty("user.dir") + "\\lib\\";
        String libPath = System.getProperty("java.library.path");
        System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
        System.load(opencvpath + "opencv_ffmpeg342_64.dll");
    }
    public static void main(String[] args) {
        SpringApplication.run(PoolTableApplication.class, args);
    }
}
