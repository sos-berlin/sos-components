package com.sos.commons.credentialstore.keepass.exceptions;

public class SOSKeePassCredentialException extends SOSKeePassDatabaseException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassCredentialException(final String msg) {
        super(msg);
    }

    public SOSKeePassCredentialException(final String msg, Throwable t) {
        super(msg, t);
    }

    public SOSKeePassCredentialException(Throwable t) {
        super(t);
    }
}
