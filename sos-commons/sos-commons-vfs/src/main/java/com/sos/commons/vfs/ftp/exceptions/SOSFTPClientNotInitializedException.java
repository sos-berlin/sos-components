package com.sos.commons.vfs.ftp.exceptions;

import com.sos.commons.vfs.exceptions.SOSProviderException;

public class SOSFTPClientNotInitializedException extends SOSProviderException {

    private static final long serialVersionUID = 1L;

    public SOSFTPClientNotInitializedException(String msg) {
        super(msg);
    }

}
