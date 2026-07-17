package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineTransferFileSizeException extends YADEEngineTransferFileException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTransferFileSizeException(String msg, IYADEProviderDelegator delegator) {
        super(msg, YADEReturnCode.TARGET_FILESIZE_MISMATCH_ERROR, delegator);
    }
}
