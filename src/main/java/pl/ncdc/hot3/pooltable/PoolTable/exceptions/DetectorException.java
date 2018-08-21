package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class DetectorException extends Exception {
    public DetectorException() {
        super();
    }

    public DetectorException(String message) {
        super(message);
    }

    public DetectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DetectorException(Throwable cause) {
        super(cause);
    }

    protected DetectorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
