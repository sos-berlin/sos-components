package com.sos.commons.xml.exception;

import com.sos.commons.exception.SOSException;

public class SOSDoctypeException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSDoctypeException() {
        super();
    }

    public SOSDoctypeException(String message) {
        super(message);
    }

    public SOSDoctypeException(Throwable cause) {
        super(cause);
    }

    public SOSDoctypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSDoctypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
