package com.sos.yade.engine.exceptions;

public class YADEEngineSourceConnectionException extends YADEEngineConnectionException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceConnectionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public YADEEngineSourceConnectionException(Throwable ex) {
        super(ex);
    }
}
