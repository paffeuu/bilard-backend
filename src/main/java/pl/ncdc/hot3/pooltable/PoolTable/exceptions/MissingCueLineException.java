package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class MissingCueLineException extends Exception {
    public MissingCueLineException() {
    }

    public MissingCueLineException(String message) {
        super(message);
    }

    public MissingCueLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingCueLineException(Throwable cause) {
        super(cause);
    }

    public MissingCueLineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
