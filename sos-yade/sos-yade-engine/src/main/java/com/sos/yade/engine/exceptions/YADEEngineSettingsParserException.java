package com.sos.yade.engine.exceptions;

public class YADEEngineSettingsParserException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSettingsParserException(String msg) {
        super(msg);
    }

    public YADEEngineSettingsParserException(Throwable ex) {
        super(ex);
    }

    public YADEEngineSettingsParserException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
