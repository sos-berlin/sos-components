package com.sos.yade.engine.exceptions;

public class YADEEngineDirectoryException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineDirectoryException(String msg) {
        super(msg);
    }

    public YADEEngineDirectoryException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineDirectoryException(Throwable cause) {
        super(cause);
    }
}
