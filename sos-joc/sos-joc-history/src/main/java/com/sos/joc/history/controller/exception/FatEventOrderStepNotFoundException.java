package com.sos.joc.history.controller.exception;

import com.sos.commons.exception.SOSException;

public class FatEventOrderStepNotFoundException extends SOSException {

    private static final long serialVersionUID = 1L;

    public FatEventOrderStepNotFoundException(String message) {
        super(message);
    }
}
