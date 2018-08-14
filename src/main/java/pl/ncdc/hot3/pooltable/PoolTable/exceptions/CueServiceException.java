package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class CueServiceException extends Exception {

    public CueServiceException() {
    }

    public CueServiceException(String message) {
        super(message);
    }

    public CueServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CueServiceException(Throwable cause) {
        super(cause);
    }

    public CueServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
