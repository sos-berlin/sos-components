package com.sos.yade.engine.exception;

public class SOSYADEEngineSourcePollingException extends SOSYADEEngineException {

    private static final long serialVersionUID = 1L;

    public SOSYADEEngineSourcePollingException(String msg) {
        super(msg);
    }

    public SOSYADEEngineSourcePollingException(String msg, Throwable e) {
        super(msg, e);
    }

    public SOSYADEEngineSourcePollingException(Throwable e) {
        super(e);
    }
}
