package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourceInputStreamException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceInputStreamException(Throwable e, IYADEProviderDelegator delegator) {
        super(e, YADEReturnCode.SOURCE_FILES_ERROR, delegator);
    }

}
