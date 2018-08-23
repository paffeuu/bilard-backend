package pl.ncdc.hot3.pooltable.PoolTable;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.ncdc.hot3.pooltable.PoolTable.model.PoolTable;
import pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices.OpenCVBufforFlushService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;


@SpringBootApplication(scanBasePackages={"pl.ncdc.hot3.pooltable.PoolTable.services.imageProcessingServices","pl.ncdc.hot3.pooltable"})
@EnableScheduling
public class PoolTableApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PoolTableApplication.class);
    }
    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            String opencvpath = System.getProperty("user.dir") + "\\lib\\";
            String libPath = System.getProperty("java.library.path");
            System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
            System.load(opencvpath + "opencv_ffmpeg342_64.dll");
            } else if (SystemUtils.IS_OS_LINUX) {
            System.load("/usr/local/share/OpenCV/java/" + "libopencv_java342" + ".so");
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
