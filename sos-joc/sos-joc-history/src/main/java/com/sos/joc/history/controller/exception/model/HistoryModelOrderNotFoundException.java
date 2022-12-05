package com.sos.joc.history.controller.exception.model;

public class HistoryModelOrderNotFoundException extends AHistoryModelException {

    private static final long serialVersionUID = 1L;

    public HistoryModelOrderNotFoundException(String controllerId, String message) {
        super(controllerId, message);
    }

}
