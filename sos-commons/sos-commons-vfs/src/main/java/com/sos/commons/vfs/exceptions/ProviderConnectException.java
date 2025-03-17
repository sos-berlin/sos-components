package com.sos.commons.vfs.exceptions;

public class ProviderConnectException extends ProviderException {

    private static final long serialVersionUID = 1L;

    public ProviderConnectException(Throwable cause) {
        super(cause);
    }

    public ProviderConnectException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
