package com.sos.yade.engine.exceptions;

public class YADEEngineTargetDirectoryCreationException extends YADEEngineTargetDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetDirectoryCreationException(String msg) {
        super(msg);
    }

    public YADEEngineTargetDirectoryCreationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineTargetDirectoryCreationException(Throwable cause) {
        super(cause);
    }
}
