package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;

public class YADEEngineSettingsLoadException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSettingsLoadException(String msg, YADEReturnCode returnCode) {
        super(msg, returnCode);
    }

    public YADEEngineSettingsLoadException(Throwable ex, YADEReturnCode returnCode) {
        super(ex, returnCode);
    }

    public YADEEngineSettingsLoadException(String msg, Throwable ex, YADEReturnCode returnCode) {
        super(msg, ex, returnCode);
    }
}
