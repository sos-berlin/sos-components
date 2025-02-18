package com.sos.commons.vfs.exception;

import com.sos.commons.exception.SOSException;

public class SOSProviderException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSProviderException() {
        super();
    }

    public SOSProviderException(String msg) {
        super(msg);
    }

    public SOSProviderException(Throwable cause) {
        super(cause);
    }

    public SOSProviderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
