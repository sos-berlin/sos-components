package com.sos.commons.httpclient.exception;

import org.apache.http.HttpRequest;

import com.sos.commons.exception.SOSException;

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

    public SOSConnectionResetException(HttpRequest request, Throwable cause) {
        this(String.format("[%s]%s", request.toString(), cause.toString()), cause);
    }

    public SOSConnectionResetException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSConnectionResetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
