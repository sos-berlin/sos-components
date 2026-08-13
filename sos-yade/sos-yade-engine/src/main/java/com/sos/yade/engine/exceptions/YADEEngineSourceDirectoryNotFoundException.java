package com.sos.yade.engine.exceptions;

public class YADEEngineSourceDirectoryNotFoundException extends YADEEngineSourceDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceDirectoryNotFoundException(String msg) {
        super(msg);
    }

    public YADEEngineSourceDirectoryNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineSourceDirectoryNotFoundException(Throwable cause) {
        super(cause);
    }
}
