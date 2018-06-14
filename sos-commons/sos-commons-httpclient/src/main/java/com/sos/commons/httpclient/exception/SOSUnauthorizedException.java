package com.sos.commons.httpclient.exception;

import com.sos.commons.exception.SOSException;

public class SOSUnauthorizedException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSUnauthorizedException(String message) {
        super(message);
    }

}
