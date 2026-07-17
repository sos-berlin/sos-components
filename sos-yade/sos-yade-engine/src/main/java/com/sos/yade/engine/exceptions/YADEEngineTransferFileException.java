package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineTransferFileException extends YADEEngineOperationException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTransferFileException(String msg, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, returnCode, delegator);
    }

    public YADEEngineTransferFileException(Throwable ex, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(ex, returnCode, delegator);
    }

    public YADEEngineTransferFileException(String msg, Throwable ex, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, ex, returnCode, delegator);
    }
}
