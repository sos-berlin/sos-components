package com.sos.joc.exceptions;


public class ControllerConnectionRefusedException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-402";

    public ControllerConnectionRefusedException() {
    }

    public ControllerConnectionRefusedException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerConnectionRefusedException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerConnectionRefusedException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerConnectionRefusedException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerConnectionRefusedException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerConnectionRefusedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerConnectionRefusedException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
