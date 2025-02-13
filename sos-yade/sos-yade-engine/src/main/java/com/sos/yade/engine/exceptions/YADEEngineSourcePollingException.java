package com.sos.yade.engine.exceptions;

public class YADEEngineSourcePollingException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourcePollingException(String msg) {
        super(msg);
    }

    public YADEEngineSourcePollingException(String msg, Throwable e) {
        super(msg, e);
    }

    public YADEEngineSourcePollingException(Throwable e) {
        super(e);
    }
}
