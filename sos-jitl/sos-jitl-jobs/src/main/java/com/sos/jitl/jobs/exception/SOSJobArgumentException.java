package com.sos.jitl.jobs.exception;

public class SOSJobArgumentException extends SOSJobException {

    private static final long serialVersionUID = 1L;

    public SOSJobArgumentException(String message) {
        super(message);
    }

    public SOSJobArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
