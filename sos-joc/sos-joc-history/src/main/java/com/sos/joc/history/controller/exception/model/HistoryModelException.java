package com.sos.joc.history.controller.exception.model;

public class HistoryModelException extends AHistoryModelException {

    private static final long serialVersionUID = 1L;

    public HistoryModelException(String controllerId, String message, Throwable e) {
        super(controllerId, message, e);
    }

}
