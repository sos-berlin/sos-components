package com.sos.commons.encryption.exception;

import com.sos.commons.exception.SOSException;

public class SOSEncryptionException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSEncryptionException() {
        super();
    }

    public SOSEncryptionException(String message) {
        super(message);
    }
    
    public SOSEncryptionException(Throwable cause) {
        super(cause);
    }
    
    public SOSEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSEncryptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
