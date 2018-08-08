package pooltable.PoolTable;

import imageProcessingServices.SnapshotGetterService;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
        SnapshotGetterService snap = new SnapshotGetterService();
        Mat in = snap.getLiveSnapshot();
        BufferedImage out;
        byte[] data = new byte[320 * 240 * (int)in.elemSize()];
        int type;
        in.get(0, 0, data);

        if(in.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else
            type = BufferedImage.TYPE_3BYTE_BGR;

        out = new BufferedImage(320, 240, type);

        out.getRaster().setDataElements(0, 0, 320, 240, data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( out, "jpg", baos );
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }
}
