package com.sos.jitl.jobs.common.exception;

import com.sos.commons.exception.SOSException;

public class SOSJITLJobException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSJITLJobException(String msg, Throwable e) {
        super(msg, e);
    }
}
