package com.sos.commons.job.exception;

import com.sos.commons.exception.SOSException;

public class JobException extends SOSException {

    private static final long serialVersionUID = 1L;

    public JobException(String message) {
        super(message);
    }
    
    public JobException(String msg, Throwable e) {
        super(msg, e);
    }
}
