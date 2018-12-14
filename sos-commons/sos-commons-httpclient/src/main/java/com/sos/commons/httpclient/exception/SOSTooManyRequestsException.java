package com.sos.commons.httpclient.exception;

import com.sos.commons.exception.SOSException;

public class SOSTooManyRequestsException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSTooManyRequestsException(String message) {
        super(message);
    }
}
