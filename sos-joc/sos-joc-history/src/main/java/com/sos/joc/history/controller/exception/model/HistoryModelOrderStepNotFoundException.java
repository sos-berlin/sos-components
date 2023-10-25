package com.sos.joc.history.controller.exception.model;

public class HistoryModelOrderStepNotFoundException extends HistoryModelOrderStepException {

    private static final long serialVersionUID = 1L;

    public HistoryModelOrderStepNotFoundException(String controllerId, String message) {
        super(controllerId, message);
    }
}
