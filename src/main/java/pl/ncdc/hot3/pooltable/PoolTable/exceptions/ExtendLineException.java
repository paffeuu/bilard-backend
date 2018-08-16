package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class ExtendLineException extends LineServiceException {
    public ExtendLineException() {
    }

    public ExtendLineException(String message) {
        super(message);
    }

    public ExtendLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtendLineException(Throwable cause) {
        super(cause);
    }

    public ExtendLineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
