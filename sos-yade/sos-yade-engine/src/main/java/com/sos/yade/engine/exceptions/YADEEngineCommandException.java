package com.sos.yade.engine.exceptions;

import com.sos.commons.util.beans.SOSCommandResult;

public class YADEEngineCommandException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineCommandException(String msg, SOSCommandResult result) {
        super(msg + result.toString());
    }

    public YADEEngineCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
