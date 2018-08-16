package pl.ncdc.hot3.pooltable.PoolTable;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages={"pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices","pl.ncdc.hot3.pooltable"})
@EnableScheduling



public class PoolTableApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PoolTableApplication.class);
    }

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
