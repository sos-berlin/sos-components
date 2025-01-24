package com.sos.yade.engine.exception;

import com.sos.commons.exception.SOSException;

public class SOSYADEEngineException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSYADEEngineException(String msg) {
        super(msg);
    }

    public SOSYADEEngineException(String msg, Throwable e) {
        super(msg, e);
    }

    public SOSYADEEngineException(Throwable e) {
        super(e);
    }

}
