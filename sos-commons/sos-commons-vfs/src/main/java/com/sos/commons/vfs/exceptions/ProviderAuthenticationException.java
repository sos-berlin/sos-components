package com.sos.commons.vfs.exceptions;

public class ProviderAuthenticationException extends ProviderException {

    private static final long serialVersionUID = 1L;

    public ProviderAuthenticationException(String message) {
        super(message);
    }

    public ProviderAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderAuthenticationException(Throwable cause) {
        super(cause);
    }
}
