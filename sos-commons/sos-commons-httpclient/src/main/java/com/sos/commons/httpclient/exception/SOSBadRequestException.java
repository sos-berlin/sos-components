package com.sos.commons.httpclient.exception;

import com.sos.commons.exception.SOSException;

public class SOSBadRequestException extends SOSException {

    private static final long serialVersionUID = 1L;
    private int httpCode = 400;
    
    public int getHttpCode() {
        return httpCode;
    }
    
    public SOSBadRequestException() {
        super();
    }

    public SOSBadRequestException(String message) {
        super(message);
    }
    
    public SOSBadRequestException(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }
    
    public SOSBadRequestException(Throwable cause) {
        super(cause);
    }
    
    public SOSBadRequestException(int httpCode, Throwable cause) {
        super(cause);
        this.httpCode = httpCode;
    }
    
    public SOSBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SOSBadRequestException(int httpCode, String message, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
    }

    public SOSBadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public SOSBadRequestException(int httpCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.httpCode = httpCode;
    }
}
