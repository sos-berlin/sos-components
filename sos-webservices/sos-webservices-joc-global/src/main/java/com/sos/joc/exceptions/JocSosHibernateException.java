package com.sos.joc.exceptions;


public class JocSosHibernateException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-408";

    public JocSosHibernateException() {
    }

    public JocSosHibernateException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocSosHibernateException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocSosHibernateException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocSosHibernateException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocSosHibernateException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocSosHibernateException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocSosHibernateException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
