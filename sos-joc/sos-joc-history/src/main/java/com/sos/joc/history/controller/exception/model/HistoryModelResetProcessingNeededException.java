package com.sos.joc.history.controller.exception.model;

public class HistoryModelResetProcessingNeededException extends AHistoryModelException {

    private static final long serialVersionUID = 1L;

    public HistoryModelResetProcessingNeededException(String controllerId, String message) {
        super(controllerId, message);
    }

}
