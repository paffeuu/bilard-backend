package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class TunnelMakerException extends Exception {
    public TunnelMakerException() {
    }

    public TunnelMakerException(String message) {
        super(message);
    }

    public TunnelMakerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TunnelMakerException(Throwable cause) {
        super(cause);
    }

    public TunnelMakerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
