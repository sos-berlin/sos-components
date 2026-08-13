package com.sos.yade.engine.exceptions;

public class YADEEngineTargetDirectoryNotFoundException extends YADEEngineTargetDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetDirectoryNotFoundException(String msg) {
        super(msg);
    }

    public YADEEngineTargetDirectoryNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineTargetDirectoryNotFoundException(Throwable cause) {
        super(cause);
    }
}
