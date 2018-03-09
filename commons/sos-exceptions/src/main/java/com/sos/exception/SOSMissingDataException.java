package com.sos.exception;


public class SOSMissingDataException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSMissingDataException() {
        super();
    }

    public SOSMissingDataException(String message) {
        super(message);
    }
    
    public SOSMissingDataException(Throwable cause) {
        super(cause);
    }
    
    public SOSMissingDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSMissingDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
