package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourceRemoveFileException extends YADEEngineOperationException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceRemoveFileException(String msg, Throwable ex, IYADEProviderDelegator delegator) {
        super(msg, ex, YADEReturnCode.SOURCE_FILES_ERROR, delegator);
    }

    public YADEEngineSourceRemoveFileException(Throwable ex, IYADEProviderDelegator delegator) {
        super(ex, YADEReturnCode.SOURCE_FILES_ERROR, delegator);
    }
}
