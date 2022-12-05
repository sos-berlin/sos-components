package com.sos.joc.history.controller.exception;

public class HistoryProcessingDatabaseConnectException extends HistoryProcessingException {

    private static final long serialVersionUID = 1L;

    public HistoryProcessingDatabaseConnectException(String controllerId, Throwable e) {
        super(controllerId, e);
    }

}
