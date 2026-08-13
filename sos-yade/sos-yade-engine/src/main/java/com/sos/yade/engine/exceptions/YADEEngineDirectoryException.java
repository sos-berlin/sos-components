package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;

public class YADEEngineDirectoryException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineDirectoryException(String msg) {
        super(msg,YADEReturnCode.DEFAULT_ERROR);
    }

    public YADEEngineDirectoryException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public YADEEngineDirectoryException(Throwable cause) {
        super(cause);
    }
}
