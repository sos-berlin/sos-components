package com.sos.commons.vfs.exceptions;

public class ProviderConnectException extends ProviderException {

    private static final long serialVersionUID = 1L;

    boolean authenticationException = false;

    public ProviderConnectException(Throwable cause) {
        super(cause);
    }

    public ProviderConnectException(String msg) {
        super(msg);
    }

    public ProviderConnectException(String msg, ProviderConnectException cause) {
        super(msg, cause);
        authenticationException = cause.isAuthenticationException();
    }

    public ProviderConnectException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public boolean isAuthenticationException() {
        return authenticationException;
    }

    protected void setAuthenticationException() {
        authenticationException = true;
    }
}
