package com.sos.joc.exceptions;


public class JocNotImplementedException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-004";

    public JocNotImplementedException() {
    }

    public JocNotImplementedException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocNotImplementedException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocNotImplementedException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocNotImplementedException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocNotImplementedException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocNotImplementedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocNotImplementedException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
