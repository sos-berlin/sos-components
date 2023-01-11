package com.sos.joc.exceptions;


public class JocAccessDeniedException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-401";

    public JocAccessDeniedException() {
    }

    public JocAccessDeniedException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocAccessDeniedException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocAccessDeniedException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocAccessDeniedException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocAccessDeniedException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocAccessDeniedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocAccessDeniedException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
