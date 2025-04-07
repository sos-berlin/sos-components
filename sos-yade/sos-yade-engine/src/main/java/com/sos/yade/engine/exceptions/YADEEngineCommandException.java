package com.sos.yade.engine.exceptions;

public class YADEEngineCommandException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    
    public YADEEngineCommandException(String prefix, int exitCode, String std) {
        super(prefix + "[exitCode="+exitCode+"]"+std);
    }

    public YADEEngineCommandException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
