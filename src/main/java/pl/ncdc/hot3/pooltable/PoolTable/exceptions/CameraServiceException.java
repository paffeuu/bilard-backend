package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class CameraServiceException extends Exception {
    public CameraServiceException() {
    }

    public CameraServiceException(String message) {
        super(message);
    }

    public CameraServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CameraServiceException(Throwable cause) {
        super(cause);
    }

    public CameraServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
