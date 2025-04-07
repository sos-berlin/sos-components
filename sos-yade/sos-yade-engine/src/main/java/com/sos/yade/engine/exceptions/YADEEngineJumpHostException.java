package com.sos.yade.engine.exceptions;

public class YADEEngineJumpHostException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineJumpHostException(String msg) {
        super(msg);
    }

    public YADEEngineJumpHostException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
