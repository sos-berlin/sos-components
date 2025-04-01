package com.sos.yade.engine.exceptions;

public class YADEEngineSettingsLoadException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSettingsLoadException(String msg) {
        super(msg);
    }

    public YADEEngineSettingsLoadException(Throwable ex) {
        super(ex);
    }

    public YADEEngineSettingsLoadException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
