package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

@Service
public class PathService {

    public final String BASE_PATH = this.getClass().getClassLoader().getResource("emptyTable.png").toString();
    public final String TESTS_PATH = "src/main/resources/";

    public String getFullPath(String filename) throws FileNotFoundException {
        String path = this.getClass().getClassLoader().getResource(filename).getPath().replaceAll("%20"," ").substring(1);
        return path;
    }

}
