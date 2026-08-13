package com.sos.commons.vfs.exceptions;

public class ProviderDirectoryCreationException extends ProviderDirectoryException {

    private static final long serialVersionUID = 1L;

    public ProviderDirectoryCreationException(String msg) {
        super(msg);
    }

    public ProviderDirectoryCreationException(String msg, Throwable e) {
        super(msg, e);
    }

}
