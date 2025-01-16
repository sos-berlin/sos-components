package com.sos.commons.vfs.exception;

public class SOSProviderConnectException extends SOSProviderException {

    private static final long serialVersionUID = 1L;

    public SOSProviderConnectException(Throwable cause) {
        super(cause);
    }

    public SOSProviderConnectException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
