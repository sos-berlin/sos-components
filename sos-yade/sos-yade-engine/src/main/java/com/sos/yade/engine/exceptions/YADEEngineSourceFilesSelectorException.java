package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourceFilesSelectorException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceFilesSelectorException(String msg, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, returnCode, delegator);
    }

    public YADEEngineSourceFilesSelectorException(String msg, Throwable cause, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, cause, returnCode, delegator);
    }

    public YADEEngineSourceFilesSelectorException(Throwable cause, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(cause, returnCode, delegator);
    }
}
