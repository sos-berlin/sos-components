package com.sos.yade.engine.exceptions;

public class YADEEngineSourceSteadyFilesException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceSteadyFilesException(String msg) {
        super(msg);
    }

    public YADEEngineSourceSteadyFilesException(String msg, Throwable e) {
        super(msg, e);
    }

    public YADEEngineSourceSteadyFilesException(Throwable e) {
        super(e);
    }
}
