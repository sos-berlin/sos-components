package com.sos.yade.engine.exceptions;

public class YADEEngineTransferFileIntegrityHashViolationException extends YADEEngineTransferFileException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTransferFileIntegrityHashViolationException(String msg) {
        super(msg);
    }
}
