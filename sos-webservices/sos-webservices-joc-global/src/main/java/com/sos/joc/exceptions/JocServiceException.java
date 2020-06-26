package com.sos.joc.exceptions;


public class JocServiceException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-503";

    public JocServiceException() {
    }

    public JocServiceException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocServiceException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocServiceException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocServiceException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocServiceException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocServiceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocServiceException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
