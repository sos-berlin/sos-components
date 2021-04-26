package com.sos.jitl.jobs.exception;

import com.sos.commons.exception.SOSException;

public class SOSJobException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSJobException(String message) {
        super(message);
    }

    public SOSJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
