package com.sos.commons.job.exception;

public class JobArgumentException extends JobException {

    private static final long serialVersionUID = 1L;

    public JobArgumentException(String message) {
        super(message);
    }

    public JobArgumentException(String msg, Throwable e) {
        super(msg, e);
    }
}
