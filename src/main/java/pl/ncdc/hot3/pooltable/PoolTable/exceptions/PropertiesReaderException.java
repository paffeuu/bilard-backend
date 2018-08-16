package pl.ncdc.hot3.pooltable.PoolTable.exceptions;

public class PropertiesReaderException extends Exception{

	    public PropertiesReaderException() {
	        super();
	    }

	    public PropertiesReaderException(String message) {
	        super(message);
	    }

	    public PropertiesReaderException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    public PropertiesReaderException(Throwable cause) {
	        super(cause);
	    }

	    protected PropertiesReaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	        super(message, cause, enableSuppression, writableStackTrace);
	    }
	}


