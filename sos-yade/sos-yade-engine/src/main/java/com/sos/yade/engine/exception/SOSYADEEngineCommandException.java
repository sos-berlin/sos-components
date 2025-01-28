package com.sos.yade.engine.exception;

import com.sos.commons.util.common.SOSCommandResult;

public class SOSYADEEngineCommandException extends SOSYADEEngineException {

    private static final long serialVersionUID = 2369285779503834825L;

    public SOSYADEEngineCommandException(String msg, SOSCommandResult result) {
        super(msg + result.toString());
    }

    public SOSYADEEngineCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
