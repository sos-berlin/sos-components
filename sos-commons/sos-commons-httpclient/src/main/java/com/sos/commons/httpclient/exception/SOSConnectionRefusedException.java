package com.sos.commons.httpclient.exception;

import java.net.http.HttpRequest;

import com.sos.commons.exception.SOSException;

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

    public SOSConnectionRefusedException(HttpRequest request, Throwable cause) {
        this(String.format("[%s]%s", request.toString(), cause.toString()), cause);
    }

    public SOSConnectionRefusedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSConnectionRefusedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
