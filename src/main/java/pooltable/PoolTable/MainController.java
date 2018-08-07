package pooltable.PoolTable;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;


@Controller
@RequestMapping(path="/pooltable")
public class MainController {
    @Autowired
    private ServletContext servletContext;

    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(produces = MediaType.IMAGE_JPEG_VALUE, value = "/image-byte-array", method = RequestMethod.GET)
    public @ResponseBody byte[] getPoolTableImage() throws IOException {
        InputStream in = servletContext.getResourceAsStream("/WEB-INF/table_cut01.jpg"); // zmienić na obraz zwracany przez metode
        return IOUtils.toByteArray(in);
    }
}
