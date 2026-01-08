package com.sos.yade.engine.exceptions;

public class YADEEngineInvalidExpressionException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineInvalidExpressionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public YADEEngineInvalidExpressionException(String msg, YADEEngineInvalidExpressionException ex) {
        super(msg + ex.getMessage() + ex.getCause());
    }
}
