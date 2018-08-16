package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class CameraLoadingException extends CameraServiceException {
    public CameraLoadingException() {
    }

    public CameraLoadingException(String message) {
        super(message);
    }

    public CameraLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CameraLoadingException(Throwable cause) {
        super(cause);
    }

    public CameraLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
