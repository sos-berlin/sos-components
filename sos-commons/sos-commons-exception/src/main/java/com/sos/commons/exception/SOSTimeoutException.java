package com.sos.commons.exception;

public class SOSTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public SOSTimeoutException() {
        super();
    }

    public SOSTimeoutException(String message) {
        super(message);
    }
    
    public SOSTimeoutException(Throwable cause) {
        super(cause);
    }
    
    public SOSTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
