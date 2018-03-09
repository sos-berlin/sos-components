package com.sos.exception;


public class SOSInvalidDataException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSInvalidDataException() {
        super();
    }

    public SOSInvalidDataException(String message) {
        super(message);
    }
    
    public SOSInvalidDataException(Throwable cause) {
        super(cause);
    }
    
    public SOSInvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSInvalidDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
