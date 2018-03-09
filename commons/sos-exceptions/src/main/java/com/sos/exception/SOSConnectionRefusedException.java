package com.sos.exception;


public class SOSConnectionRefusedException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSConnectionRefusedException() {
        super();
    }

    public SOSConnectionRefusedException(String message) {
        super(message);
    }
    
    public SOSConnectionRefusedException(Throwable cause) {
        super(cause);
    }
    
    public SOSConnectionRefusedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSConnectionRefusedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
