package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;

public class YADEEngineTargetDirectoryCreationException extends YADEEngineTargetDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetDirectoryCreationException(String msg) {
        super(msg, YADEReturnCode.TARGET_DIRECTORY_CREATION_ERROR);
    }

    public YADEEngineTargetDirectoryCreationException(String msg, Throwable cause) {
        super(msg, cause, YADEReturnCode.TARGET_DIRECTORY_CREATION_ERROR);
    }

    public YADEEngineTargetDirectoryCreationException(Throwable cause) {
        super(cause, YADEReturnCode.TARGET_DIRECTORY_CREATION_ERROR);
    }
}
