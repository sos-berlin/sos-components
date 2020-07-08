package com.sos.joc.exceptions;


public class UnknownJobSchedulerControllerException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-404";

    public UnknownJobSchedulerControllerException() {
    }

    public UnknownJobSchedulerControllerException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public UnknownJobSchedulerControllerException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public UnknownJobSchedulerControllerException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public UnknownJobSchedulerControllerException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public UnknownJobSchedulerControllerException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public UnknownJobSchedulerControllerException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public UnknownJobSchedulerControllerException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
