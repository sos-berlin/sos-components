package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourcePollingException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourcePollingException(String msg, IYADEProviderDelegator delegator) {
        super(msg, delegator);
    }

    public YADEEngineSourcePollingException(String msg, Throwable e, IYADEProviderDelegator delegator) {
        super(msg, e, delegator);
    }

    public YADEEngineSourcePollingException(Throwable e, IYADEProviderDelegator delegator) {
        super(e, delegator);
    }
}
