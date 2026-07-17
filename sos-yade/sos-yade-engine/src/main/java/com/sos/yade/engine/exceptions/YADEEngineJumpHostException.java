package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineJumpHostException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineJumpHostException(String msg, IYADEProviderDelegator delegator) {
        super(msg, delegator);
    }

    public YADEEngineJumpHostException(String msg, Throwable cause, IYADEProviderDelegator delegator) {
        super(msg, cause, delegator);
    }
}
