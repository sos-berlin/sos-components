package com.sos.commons.vfs.exceptions;

public class ProviderNoSuchFileException extends ProviderException {

    private static final long serialVersionUID = 1L;

    public ProviderNoSuchFileException(String msg) {
        super(msg);
    }
}
