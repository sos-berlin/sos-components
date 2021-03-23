package com.sos.joc.exceptions;


public class ControllerServiceUnavailableException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-503";

    public ControllerServiceUnavailableException() {
    }

    public ControllerServiceUnavailableException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerServiceUnavailableException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerServiceUnavailableException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerServiceUnavailableException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerServiceUnavailableException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerServiceUnavailableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerServiceUnavailableException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
