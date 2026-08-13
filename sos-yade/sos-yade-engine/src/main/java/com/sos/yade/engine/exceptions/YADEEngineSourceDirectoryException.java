package com.sos.yade.engine.exceptions;

public class YADEEngineSourceDirectoryException extends YADEEngineDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceDirectoryException(String msg) {
        super(msg);
    }

    public YADEEngineSourceDirectoryException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineSourceDirectoryException(Throwable cause) {
        super(cause);
    }
}
