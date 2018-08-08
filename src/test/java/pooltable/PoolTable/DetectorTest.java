package pooltable.PoolTable;

import org.junit.Test;
import org.opencv.core.Core;
import pooltable.exceptions.DetectorException;

public class DetectorTest {

    @Test(expected = DetectorException.class)
    public void getLinesShouldThrowExceptionWhenSourceNull() throws DetectorException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Detector detector = new Detector();

        detector.setSourceImg(null);
        detector.findStickLine();
    }

    @Test
    public void getStickShouldReturnDetectedStickFor2SourceViews(){



    }

}
