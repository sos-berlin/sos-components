package com.sos.yade.engine.exceptions;

import com.sos.commons.util.common.SOSCommandResult;

public class SOSYADEEngineCommandException extends SOSYADEEngineException {

    private static final long serialVersionUID = 1L;

    public SOSYADEEngineCommandException(String msg, SOSCommandResult result) {
        super(msg + result.toString());
    }

    public SOSYADEEngineCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
