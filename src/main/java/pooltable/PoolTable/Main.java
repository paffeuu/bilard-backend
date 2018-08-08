package pooltable.PoolTable;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {

	 public static void main(String[] args) throws IOException {
		 
		 System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        File input = new File("C:\\Users\\Borat\\Pictures\\pool2.png");
        BufferedImage image = ImageIO.read(input);         
     	byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();            
     	Mat img = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
     	img.put(0, 0, data);

     	Detector detec = new Detector();
     	detec.setSourceImg(img);
     	detec.drawBalls();

     	Imgcodecs.imwrite("C:\\Users\\Borat\\Pictures\\pool1edited.png", detec.getSourceImg());
	}
}
