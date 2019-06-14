package com.sos.commons.httpclient.exception;

import org.apache.http.HttpRequest;

import com.sos.commons.exception.SOSException;

public class SOSNoResponseException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSNoResponseException() {
        super();
    }

    public SOSNoResponseException(String message) {
        super(message);
    }

    public SOSNoResponseException(Throwable cause) {
        super(cause);
    }

    public SOSNoResponseException(HttpRequest request, Throwable cause) {
        this(String.format("[%s]%s", request.toString(), cause.toString()), cause);
    }

    public SOSNoResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSNoResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
