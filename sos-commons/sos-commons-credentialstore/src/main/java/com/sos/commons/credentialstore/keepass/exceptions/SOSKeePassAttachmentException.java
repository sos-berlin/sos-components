package com.sos.commons.credentialstore.keepass.exceptions;

public class SOSKeePassAttachmentException extends SOSKeePassDatabaseException {

    private static final long serialVersionUID = 1L;

    public SOSKeePassAttachmentException(final String msg) {
        super(msg);
    }

    public SOSKeePassAttachmentException(final String msg, Throwable t) {
        super(msg, t);
    }

    public SOSKeePassAttachmentException(Throwable t) {
        super(t);
    }
}
