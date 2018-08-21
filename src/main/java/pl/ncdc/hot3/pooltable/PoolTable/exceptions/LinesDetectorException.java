package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class LinesDetectorException extends DetectorException {
    public LinesDetectorException() {
    }

    public LinesDetectorException(String message) {
        super(message);
    }

    public LinesDetectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinesDetectorException(Throwable cause) {
        super(cause);
    }

    public LinesDetectorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
