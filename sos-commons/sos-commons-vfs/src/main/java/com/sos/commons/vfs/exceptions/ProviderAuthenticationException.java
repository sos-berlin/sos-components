package com.sos.commons.vfs.exceptions;

/** All providers */
public class ProviderAuthenticationException extends ProviderConnectException {

    private static final long serialVersionUID = 1L;

    public ProviderAuthenticationException(String message) {
        super(message);
        setAuthenticationException();
    }

    public ProviderAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        setAuthenticationException();
    }

    public ProviderAuthenticationException(Throwable cause) {
        super(cause);
        setAuthenticationException();
    }
}
