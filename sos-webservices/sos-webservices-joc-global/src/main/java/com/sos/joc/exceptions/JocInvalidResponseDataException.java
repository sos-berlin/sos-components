package com.sos.joc.exceptions;


public class JocInvalidResponseDataException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-405";

    public JocInvalidResponseDataException() {
    }
    
    public JocInvalidResponseDataException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocInvalidResponseDataException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocInvalidResponseDataException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocInvalidResponseDataException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocInvalidResponseDataException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocInvalidResponseDataException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocInvalidResponseDataException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
