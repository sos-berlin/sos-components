package com.sos.commons.httpclient.exception;

import com.sos.commons.exception.SOSException;

public class SOSForbiddenException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSForbiddenException(String message) {
        super(message);
    }

}
