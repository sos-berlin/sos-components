package com.sos.yade.engine.exceptions;

public class YADEEngineInitializationException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineInitializationException(Throwable cause) {
        super(cause);
    }

    public YADEEngineInitializationException(String msg) {
        super(msg);
    }
}
