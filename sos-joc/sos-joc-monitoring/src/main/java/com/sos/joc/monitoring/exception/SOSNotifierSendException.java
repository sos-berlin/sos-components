package com.sos.joc.monitoring.exception;

import com.sos.commons.exception.SOSException;

public class SOSNotifierSendException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSNotifierSendException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
