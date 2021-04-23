package com.sos.commons.credentialstore.keepass.exceptions;

public class SOSKeePassEntryNotFoundException extends SOSKeePassDatabaseException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassEntryNotFoundException(final String msg) {
        super(msg);
    }
}
