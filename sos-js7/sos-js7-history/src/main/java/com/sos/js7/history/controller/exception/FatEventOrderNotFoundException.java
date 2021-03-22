package com.sos.js7.history.controller.exception;

import com.sos.commons.exception.SOSException;

public class FatEventOrderNotFoundException extends SOSException {

    private static final long serialVersionUID = 1L;

    public FatEventOrderNotFoundException(String message) {
        super(message);
    }

}
