package com.sos.joc.history.controller.exception.model;

public class HistoryModelOrderException extends AHistoryModelException {

    private static final long serialVersionUID = 1L;

    public HistoryModelOrderException(String controllerId, String message) {
        super(controllerId, message);
    }

    public HistoryModelOrderException(String controllerId, String message, Throwable e) {
        super(controllerId, message, e);
    }

}
