package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class DrawerException extends Exception {
    public DrawerException() {
    }

    public DrawerException(String message) {
        super(message);
    }

    public DrawerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrawerException(Throwable cause) {
        super(cause);
    }

    public DrawerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
