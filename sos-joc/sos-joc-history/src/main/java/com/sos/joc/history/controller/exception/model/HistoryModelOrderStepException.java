package com.sos.joc.history.controller.exception.model;

public class HistoryModelOrderStepException extends AHistoryModelException {

    private static final long serialVersionUID = 1L;

    public HistoryModelOrderStepException(String controllerId, String message) {
        super(controllerId, message);
    }

    public HistoryModelOrderStepException(String controllerId, String message, Throwable e) {
        super(controllerId, message, e);
    }
}
