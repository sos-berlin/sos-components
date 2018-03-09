package com.sos.exception;


public class SOSConnectionResetException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSConnectionResetException() {
        super();
    }

    public SOSConnectionResetException(String message) {
        super(message);
    }
    
    public SOSConnectionResetException(Throwable cause) {
        super(cause);
    }
    
    public SOSConnectionResetException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSConnectionResetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
