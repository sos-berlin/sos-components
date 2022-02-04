package com.sos.commons.credentialstore.keepass.exceptions;

public class SOSKeePassKeyFileParseException extends SOSKeePassKeyFileException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassKeyFileParseException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
