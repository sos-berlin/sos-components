package com.sos.joc.history.controller.exception;

import com.sos.joc.history.controller.exception.model.HistoryModelResetProcessingNeededException;

public class HistoryProcessingResetProcessingNeededException extends HistoryProcessingException {

    private static final long serialVersionUID = 1L;

    public HistoryProcessingResetProcessingNeededException(HistoryModelResetProcessingNeededException e) {
        super(e.getControllerId(), e);
    }

}
