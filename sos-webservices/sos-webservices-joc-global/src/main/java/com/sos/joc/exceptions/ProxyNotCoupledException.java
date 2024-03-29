package com.sos.joc.exceptions;


public class ProxyNotCoupledException extends ControllerConnectionRefusedException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-402";

    public ProxyNotCoupledException() {
    }

    public ProxyNotCoupledException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ProxyNotCoupledException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ProxyNotCoupledException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ProxyNotCoupledException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ProxyNotCoupledException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ProxyNotCoupledException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ProxyNotCoupledException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
