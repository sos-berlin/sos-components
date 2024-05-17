package com.sos.exception;

import com.sos.commons.exception.SOSException;

public class SOSKeyException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSKeyException() {
        super();
    }

    public SOSKeyException(String message) {
        super(message);
    }
    
    public SOSKeyException(Throwable cause) {
        super(cause);
    }
    
    public SOSKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
