package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourceSteadyFilesException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceSteadyFilesException(String msg, IYADEProviderDelegator delegator) {
        super(msg, delegator);
    }

    public YADEEngineSourceSteadyFilesException(String msg, Throwable e, IYADEProviderDelegator delegator) {
        super(msg, e, delegator);
    }

    public YADEEngineSourceSteadyFilesException(Throwable e, IYADEProviderDelegator delegator) {
        super(e, delegator);
    }
}
