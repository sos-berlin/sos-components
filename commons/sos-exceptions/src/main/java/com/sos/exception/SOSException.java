package com.sos.exception;


public class SOSException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public SOSException() {
        super();
    }

    public SOSException(String message) {
        super(message);
    }
    
    public SOSException(Throwable cause) {
        super(cause);
    }
    
    public SOSException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
