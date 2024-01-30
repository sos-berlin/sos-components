package com.sos.joc.exceptions;


public class JocConcurrentAccessException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-425";

    public JocConcurrentAccessException() {
    }

    public JocConcurrentAccessException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocConcurrentAccessException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocConcurrentAccessException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocConcurrentAccessException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocConcurrentAccessException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocConcurrentAccessException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocConcurrentAccessException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
