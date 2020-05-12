package com.sos.joc.exceptions;


public class JocUnsupportedKeyTypeException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-414";

    public JocUnsupportedKeyTypeException() {
    }

    public JocUnsupportedKeyTypeException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocUnsupportedKeyTypeException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocUnsupportedKeyTypeException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocUnsupportedKeyTypeException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocUnsupportedKeyTypeException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocUnsupportedKeyTypeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocUnsupportedKeyTypeException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
