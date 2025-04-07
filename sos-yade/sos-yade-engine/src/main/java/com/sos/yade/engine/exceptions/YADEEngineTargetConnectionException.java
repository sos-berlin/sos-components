package com.sos.yade.engine.exceptions;

public class YADEEngineTargetConnectionException extends YADEEngineConnectionException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetConnectionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public YADEEngineTargetConnectionException(Throwable ex) {
        super(ex);
    }
}
