package com.sos.commons.exception;

public class SOSTimeoutExeededException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSTimeoutExeededException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
