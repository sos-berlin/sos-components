package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineOperationException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineOperationException(String msg, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, returnCode, delegator);
    }

    public YADEEngineOperationException(Throwable ex, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(ex, returnCode, delegator);
    }

    public YADEEngineOperationException(String msg, Throwable ex, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, ex, returnCode, delegator);
    }
}
