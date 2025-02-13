package com.sos.yade.engine.exceptions;

public class YADEEngineSourceZeroByteFilesException extends YADEEngineSourceFilesSelectorException {

    private static final long serialVersionUID = 1L;

    public YADEEngineSourceZeroByteFilesException(String msg) {
        super(msg);
    }
}
