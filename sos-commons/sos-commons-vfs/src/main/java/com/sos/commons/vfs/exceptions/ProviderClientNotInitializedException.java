package com.sos.commons.vfs.exceptions;

public class ProviderClientNotInitializedException extends ProviderException {

    private static final long serialVersionUID = 1L;

    public ProviderClientNotInitializedException(String prefix, Class<?> clientClazz, String method) {
        super(prefix + "[" + clientClazz.getName() + "]" + method);
    }

}
