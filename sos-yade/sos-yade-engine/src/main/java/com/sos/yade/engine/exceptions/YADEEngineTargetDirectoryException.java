package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;

public class YADEEngineTargetDirectoryException extends YADEEngineDirectoryException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetDirectoryException(String msg) {
        super(msg, YADEReturnCode.TARGET_DIRECTORY_ERROR);
    }

    public YADEEngineTargetDirectoryException(String msg, YADEReturnCode returnCode) {
        super(msg, returnCode);
    }

    public YADEEngineTargetDirectoryException(String msg, Throwable cause) {
        super(msg, cause, YADEReturnCode.TARGET_DIRECTORY_ERROR);
    }

    public YADEEngineTargetDirectoryException(String msg, Throwable cause, YADEReturnCode returnCode) {
        super(msg, cause, returnCode);
    }

    public YADEEngineTargetDirectoryException(Throwable cause) {
        super(cause, YADEReturnCode.TARGET_DIRECTORY_ERROR);
    }

    public YADEEngineTargetDirectoryException(Throwable cause, YADEReturnCode returnCode) {
        super(cause, returnCode);
    }

    public YADEEngineTargetDirectoryException(String msg, Throwable e, AYADEProviderDelegator delegator) {
        super(msg, e, YADEReturnCode.TARGET_DIRECTORY_ERROR, delegator);
    }
}
