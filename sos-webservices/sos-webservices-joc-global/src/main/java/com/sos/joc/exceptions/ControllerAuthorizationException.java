package com.sos.joc.exceptions;


public class ControllerAuthorizationException extends ProxyNotCoupledException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-402";

    public ControllerAuthorizationException() {
    }

    public ControllerAuthorizationException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerAuthorizationException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerAuthorizationException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerAuthorizationException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerAuthorizationException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerAuthorizationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerAuthorizationException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
