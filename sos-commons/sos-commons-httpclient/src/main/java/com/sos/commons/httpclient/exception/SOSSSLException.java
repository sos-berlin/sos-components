package com.sos.commons.httpclient.exception;

import com.sos.commons.exception.SOSException;

public class SOSSSLException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSSSLException() {
        super();
    }

    public SOSSSLException(String message) {
        super(message);
    }
    
    public SOSSSLException(Throwable cause) {
        super(cause);
    }
    
    public SOSSSLException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSSSLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
