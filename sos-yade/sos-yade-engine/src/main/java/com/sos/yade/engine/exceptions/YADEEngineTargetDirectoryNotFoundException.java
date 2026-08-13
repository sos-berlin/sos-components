package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;

public class YADEEngineTargetDirectoryNotFoundException extends YADEEngineTargetDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetDirectoryNotFoundException(String msg) {
        super(msg, YADEReturnCode.TARGET_DIRECTORY_NOT_FOUND);
    }

    public YADEEngineTargetDirectoryNotFoundException(String msg, Throwable cause) {
        super(msg, cause, YADEReturnCode.TARGET_DIRECTORY_NOT_FOUND);
    }

    public YADEEngineTargetDirectoryNotFoundException(Throwable cause) {
        super(cause, YADEReturnCode.TARGET_DIRECTORY_NOT_FOUND);
    }
}
