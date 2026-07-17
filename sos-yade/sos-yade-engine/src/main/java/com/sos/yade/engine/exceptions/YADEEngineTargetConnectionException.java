package com.sos.yade.engine.exceptions;

import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineTargetConnectionException extends YADEEngineConnectionException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetConnectionException(ProviderConnectException ex, IYADEProviderDelegator delegator) {
        super(ex.getMessage(), ex.getCause(), YADEEngineException.getTargetConnectionErrorReturnCode(ex), delegator);
    }
}
