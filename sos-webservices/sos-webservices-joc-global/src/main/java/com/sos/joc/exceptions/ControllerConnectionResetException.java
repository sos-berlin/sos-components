package com.sos.joc.exceptions;


public class ControllerConnectionResetException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-402";

    public ControllerConnectionResetException() {
    }

    public ControllerConnectionResetException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerConnectionResetException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerConnectionResetException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerConnectionResetException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerConnectionResetException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerConnectionResetException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerConnectionResetException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
