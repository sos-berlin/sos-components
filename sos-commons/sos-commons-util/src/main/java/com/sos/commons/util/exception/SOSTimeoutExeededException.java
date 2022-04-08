package com.sos.commons.util.exception;

import com.sos.commons.exception.SOSException;
import com.sos.commons.util.common.SOSTimeout;

public class SOSTimeoutExeededException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSTimeoutExeededException(SOSTimeout timeout, Throwable cause) {
        super(String.format("[timeout=%s]%s", timeout, cause == null ? "" : cause.toString()), cause);
    }
}
