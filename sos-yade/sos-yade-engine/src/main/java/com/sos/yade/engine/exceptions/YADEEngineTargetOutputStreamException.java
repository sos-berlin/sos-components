package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineTargetOutputStreamException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTargetOutputStreamException(String msg, IYADEProviderDelegator delegator) {
        super(msg, YADEReturnCode.TARGET_FILES_ERROR, delegator);
    }

    public YADEEngineTargetOutputStreamException(Throwable e, IYADEProviderDelegator delegator) {
        super(e, YADEReturnCode.TARGET_FILES_ERROR, delegator);
    }
}
