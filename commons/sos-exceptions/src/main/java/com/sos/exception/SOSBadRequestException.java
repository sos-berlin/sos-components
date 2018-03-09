package com.sos.exception;


public class SOSBadRequestException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSBadRequestException() {
        super();
    }

    public SOSBadRequestException(String message) {
        super(message);
    }
    
    public SOSBadRequestException(Throwable cause) {
        super(cause);
    }
    
    public SOSBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSBadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
