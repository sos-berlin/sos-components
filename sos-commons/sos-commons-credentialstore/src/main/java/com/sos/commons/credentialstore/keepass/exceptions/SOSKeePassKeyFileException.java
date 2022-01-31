package com.sos.commons.credentialstore.keepass.exceptions;

public class SOSKeePassKeyFileException extends SOSKeePassDatabaseException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassKeyFileException(final String msg) {
        super(msg);
    }

    public SOSKeePassKeyFileException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
