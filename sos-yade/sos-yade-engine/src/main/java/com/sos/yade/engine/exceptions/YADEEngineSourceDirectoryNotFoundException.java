package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;

public class YADEEngineSourceDirectoryNotFoundException extends YADEEngineSourceDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceDirectoryNotFoundException(String msg) {
        super(msg, YADEReturnCode.SOURCE_DIRECTORY_NOT_FOUND);
    }

    public YADEEngineSourceDirectoryNotFoundException(String msg, Throwable cause) {
        super(msg, cause, YADEReturnCode.SOURCE_DIRECTORY_NOT_FOUND);
    }

    public YADEEngineSourceDirectoryNotFoundException(Throwable cause) {
        super(cause, YADEReturnCode.SOURCE_DIRECTORY_NOT_FOUND);
    }
}
