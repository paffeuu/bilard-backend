package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class LineServiceException extends Exception {

    public LineServiceException() {
    }

    public LineServiceException(String message) {
        super(message);
    }

    public LineServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public LineServiceException(Throwable cause) {
        super(cause);
    }

    public LineServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
