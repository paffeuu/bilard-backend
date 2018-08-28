package pl.ncdc.hot3.pooltable.PoolTable;

import org.apache.commons.lang3.SystemUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.ImageUndistorterService;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;


@SpringBootApplication(scanBasePackages={"pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices","pl.ncdc.hot3.pooltable"})
@EnableScheduling
public class PoolTableApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PoolTableApplication.class);
    }
    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.load(Properties.getWindowsOpencvPath());
            System.load(Properties.getWindowsFfmpegPath());
            } else if (SystemUtils.IS_OS_LINUX) {
            System.load(Properties.getLinuxOpencvPath());
        }

    }
    public static void main(String[] args) {
        SpringApplication.run(PoolTableApplication.class, args);
        new Thread(() -> {
            while (true) {
                OpenCVBufforFlushService.getFrame();
            }
        }).start();
    }
}
