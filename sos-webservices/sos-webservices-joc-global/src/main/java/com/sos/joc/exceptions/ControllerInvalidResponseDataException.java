package com.sos.joc.exceptions;


public class ControllerInvalidResponseDataException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-405";

    public ControllerInvalidResponseDataException() {
    }
    
    public ControllerInvalidResponseDataException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerInvalidResponseDataException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerInvalidResponseDataException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerInvalidResponseDataException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerInvalidResponseDataException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerInvalidResponseDataException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerInvalidResponseDataException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
