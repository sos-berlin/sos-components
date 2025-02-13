package com.sos.yade.engine.exceptions;

public class YADEEngineTransferFileException extends YADEEngineOperationException {

    private static final long serialVersionUID = 1L;

    public YADEEngineTransferFileException(String msg) {
        super(msg);
    }
    
    public YADEEngineTransferFileException(Throwable ex) {
        super(ex);
    }

    public YADEEngineTransferFileException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
