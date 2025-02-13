package com.sos.yade.engine.exceptions;

public class YADEEngineOperationException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineOperationException(String msg) {
        super(msg);
    }

    public YADEEngineOperationException(Throwable ex) {
        super(ex);
    }

    public YADEEngineOperationException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
