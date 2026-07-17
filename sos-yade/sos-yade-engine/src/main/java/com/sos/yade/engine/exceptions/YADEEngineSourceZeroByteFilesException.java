package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineSourceZeroByteFilesException extends YADEEngineSourceFilesSelectorException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceZeroByteFilesException(String msg, IYADEProviderDelegator delegator) {
        super(msg, YADEReturnCode.SOURCE_FILES_ZERO_BYTES_ERROR, delegator);
    }
}
