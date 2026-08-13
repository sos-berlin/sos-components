package com.sos.yade.engine.exceptions;

public class YADEEngineTargetDirectoryException extends YADEEngineDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetDirectoryException(String msg) {
        super(msg);
    }

    public YADEEngineTargetDirectoryException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineTargetDirectoryException(Throwable cause) {
        super(cause);
    }
}
