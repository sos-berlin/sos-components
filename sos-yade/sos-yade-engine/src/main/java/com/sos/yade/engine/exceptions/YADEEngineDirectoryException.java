package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;

public class YADEEngineDirectoryException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineDirectoryException(String msg, YADEReturnCode returnCode) {
        super(msg, returnCode);
    }

    public YADEEngineDirectoryException(String msg, Throwable cause, YADEReturnCode returnCode) {
        super(msg, cause, returnCode);
    }

    public YADEEngineDirectoryException(Throwable cause, YADEReturnCode returnCode) {
        super(cause, returnCode);
    }

    public YADEEngineDirectoryException(String msg, Throwable e, YADEReturnCode returnCode, AYADEProviderDelegator delegator) {
        super(msg, e, returnCode, delegator);
    }
}
