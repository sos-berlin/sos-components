package com.sos.yade.engine.exceptions;

import com.sos.commons.exception.SOSException;

public class YADEEngineException extends SOSException {

    private static final long serialVersionUID = 1L;

    public YADEEngineException(String msg) {
        super(msg);
    }

    public YADEEngineException(String msg, Throwable e) {
        super(msg, e);
    }

    public YADEEngineException(Throwable e) {
        super(e);
    }

}
