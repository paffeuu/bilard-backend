package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class BallsDetectorException extends DetectorException {
    public BallsDetectorException() {
    }

    public BallsDetectorException(String message) {
        super(message);
    }

    public BallsDetectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public BallsDetectorException(Throwable cause) {
        super(cause);
    }

    public BallsDetectorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
