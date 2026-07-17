package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineTransferFileIntegrityHashViolationException extends YADEEngineTransferFileException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTransferFileIntegrityHashViolationException(String msg, IYADEProviderDelegator delegator) {
        super(msg, YADEReturnCode.TARGET_FILE_CHECKSUM_ERROR, delegator);
    }
}
