package com.sos.yade.engine.exceptions;

import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineJumpHostConnectionException extends YADEEngineConnectionException {

    private static final long serialVersionUID = 1L;

    public YADEEngineJumpHostConnectionException(ProviderConnectException ex, IYADEProviderDelegator delegator) {
        super(ex.getMessage(), ex.getCause(), YADEEngineException.getJumpHostConnectionErrorReturnCode(ex), delegator);
    }

}
