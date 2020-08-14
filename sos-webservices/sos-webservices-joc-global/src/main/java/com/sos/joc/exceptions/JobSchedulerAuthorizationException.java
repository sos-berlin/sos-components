package com.sos.joc.exceptions;


public class JobSchedulerAuthorizationException extends JobSchedulerConnectionRefusedException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-402";

    public JobSchedulerAuthorizationException() {
    }

    public JobSchedulerAuthorizationException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JobSchedulerAuthorizationException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JobSchedulerAuthorizationException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JobSchedulerAuthorizationException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JobSchedulerAuthorizationException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JobSchedulerAuthorizationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JobSchedulerAuthorizationException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
