package com.sos.commons.vfs.ssh.exceptions;

import com.sos.commons.vfs.exception.SOSProviderException;

public class SOSSSHClientNotInitializedException extends SOSProviderException {

    private static final long serialVersionUID = 1L;

    public SOSSSHClientNotInitializedException(String msg) {
        super(msg);
    }

}
