package com.sos.yade.engine.exceptions;

public class YADEEngineJumpHostConnectionException extends YADEEngineConnectionException {

    private static final long serialVersionUID = 1L;

    public YADEEngineJumpHostConnectionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public YADEEngineJumpHostConnectionException(Throwable ex) {
        super(ex);
    }
}
