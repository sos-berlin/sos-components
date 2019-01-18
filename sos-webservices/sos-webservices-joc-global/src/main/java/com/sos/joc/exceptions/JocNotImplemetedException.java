package com.sos.joc.exceptions;


public class JocNotImplemetedException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-004";

    public JocNotImplemetedException() {
    }

    public JocNotImplemetedException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocNotImplemetedException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocNotImplemetedException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocNotImplemetedException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocNotImplemetedException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocNotImplemetedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocNotImplemetedException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
