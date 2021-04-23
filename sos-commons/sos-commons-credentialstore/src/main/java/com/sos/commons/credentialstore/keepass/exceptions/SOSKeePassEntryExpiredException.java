package com.sos.commons.credentialstore.keepass.exceptions;

public class SOSKeePassEntryExpiredException extends SOSKeePassDatabaseException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassEntryExpiredException(final String msg) {
        super(msg);
    }
}
