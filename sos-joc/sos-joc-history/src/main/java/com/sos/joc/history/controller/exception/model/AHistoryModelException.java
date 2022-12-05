package com.sos.joc.history.controller.exception.model;

import com.sos.commons.exception.SOSException;
import com.sos.joc.history.controller.exception.IHistoryProcessingException;

public abstract class AHistoryModelException extends SOSException implements IHistoryProcessingException {

    private static final long serialVersionUID = 1L;

    private final String controllerId;

    public AHistoryModelException(String controllerId, String message) {
        this(controllerId, message, null);
    }

    public AHistoryModelException(String controllerId, String message, Throwable e) {
        super(message, e);
        this.controllerId = controllerId;
    }

    public String getControllerId() {
        return controllerId;
    }

}
