package com.sos.commons.credentialstore.keepass.exceptions;

import com.sos.commons.credentialstore.exceptions.SOSCredentialStoreException;

public class SOSKeePassDatabaseException extends SOSCredentialStoreException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassDatabaseException(final String msg) {
        super(msg);
    }

    public SOSKeePassDatabaseException(final String msg, Throwable t) {
        super(msg, t);
    }

    public SOSKeePassDatabaseException(Throwable t) {
        super(t);
    }
}
