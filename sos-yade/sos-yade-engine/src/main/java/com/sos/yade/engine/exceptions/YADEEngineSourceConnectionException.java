package com.sos.yade.engine.exceptions;

import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourceConnectionException extends YADEEngineConnectionException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceConnectionException(ProviderConnectException ex, IYADEProviderDelegator delegator) {
        super(ex.getMessage(), ex.getCause(), YADEEngineException.getSourceConnectionErrorReturnCode(ex), delegator);
    }

    public YADEEngineSourceConnectionException(Exception ex, IYADEProviderDelegator delegator) {
        super(ex.getMessage(), ex.getCause(), getSourceConnectionErrorReturnCode(ex), delegator);
    }

    private static YADEReturnCode getSourceConnectionErrorReturnCode(Exception ex) {
        if (ex instanceof ProviderConnectException) {
            return YADEEngineException.getSourceConnectionErrorReturnCode((ProviderConnectException) ex);
        }
        return YADEReturnCode.SOURCE_CONNECTION_ERROR;
    }
}
