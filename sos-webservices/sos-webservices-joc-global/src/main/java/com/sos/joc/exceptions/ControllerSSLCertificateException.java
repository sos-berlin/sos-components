package com.sos.joc.exceptions;


public class ControllerSSLCertificateException extends ControllerAuthorizationException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-402";

    public ControllerSSLCertificateException() {
    }

    public ControllerSSLCertificateException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerSSLCertificateException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerSSLCertificateException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerSSLCertificateException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerSSLCertificateException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerSSLCertificateException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerSSLCertificateException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
