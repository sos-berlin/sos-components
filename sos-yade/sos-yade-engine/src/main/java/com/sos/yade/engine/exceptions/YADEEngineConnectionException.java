package com.sos.yade.engine.exceptions;

public class YADEEngineConnectionException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineConnectionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public YADEEngineConnectionException(Throwable ex) {
        super(ex);
    }
}
