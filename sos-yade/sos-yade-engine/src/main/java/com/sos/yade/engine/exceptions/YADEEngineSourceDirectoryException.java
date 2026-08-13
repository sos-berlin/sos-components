package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;

public class YADEEngineSourceDirectoryException extends YADEEngineDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceDirectoryException(String msg) {
        super(msg, YADEReturnCode.SOURCE_DIRECTORY_ERROR);
    }

    public YADEEngineSourceDirectoryException(String msg, YADEReturnCode returnCode) {
        super(msg, returnCode);
    }

    public YADEEngineSourceDirectoryException(String msg, Throwable cause) {
        super(msg, cause, YADEReturnCode.SOURCE_DIRECTORY_ERROR);
    }

    public YADEEngineSourceDirectoryException(String msg, Throwable cause, YADEReturnCode returnCode) {
        super(msg, cause, returnCode);
    }

    public YADEEngineSourceDirectoryException(Throwable cause) {
        super(cause, YADEReturnCode.SOURCE_DIRECTORY_ERROR);
    }

    public YADEEngineSourceDirectoryException(Throwable cause, YADEReturnCode returnCode) {
        super(cause, returnCode);
    }

    public YADEEngineSourceDirectoryException(String msg, Throwable e, AYADEProviderDelegator delegator) {
        super(msg, e, YADEReturnCode.SOURCE_DIRECTORY_ERROR, delegator);
    }
}
