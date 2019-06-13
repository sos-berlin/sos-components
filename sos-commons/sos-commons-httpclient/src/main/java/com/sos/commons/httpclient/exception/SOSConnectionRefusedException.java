package com.sos.commons.httpclient.exception;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

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

    public SOSConnectionRefusedException(HttpHost target, HttpRequest request, Throwable cause) {
        this(String.format("[%s:%s]%s", target.getHostName(), target.getPort(), cause.toString()), cause);
    }

    public SOSConnectionRefusedException(HttpUriRequest request, Throwable cause) {
        this(String.format("[%s]%s", request.getURI().getQuery(), cause.toString()), cause);
    }

    public SOSConnectionRefusedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSConnectionRefusedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
