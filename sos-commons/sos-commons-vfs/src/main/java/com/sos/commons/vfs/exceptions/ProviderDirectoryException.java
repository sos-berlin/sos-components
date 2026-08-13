package com.sos.commons.vfs.exceptions;

/** See {@link ProviderDirectoryNotFoundException} for FTP/FTPS */
public class ProviderDirectoryException extends ProviderException {

    private static final long serialVersionUID = 1L;

    public ProviderDirectoryException(String msg) {
        super(msg);
    }

    public ProviderDirectoryException(String msg, Throwable e) {
        super(msg, e);
    }

    public ProviderDirectoryException(ProviderException e) {
        super(e);
    }
}
