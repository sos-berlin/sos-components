package com.sos.joc.exceptions;

import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;

public class JocAuthenticationException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-401";
    private SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = null;

    public JocAuthenticationException() {
    }

    public JocAuthenticationException(SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer) {
        super(new JocError(ERROR_CODE, sosAuthCurrentAccountAnswer.getMessage()));
        this.sosAuthCurrentAccountAnswer = sosAuthCurrentAccountAnswer;
    }
    
    public JocAuthenticationException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocAuthenticationException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocAuthenticationException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocAuthenticationException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocAuthenticationException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocAuthenticationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocAuthenticationException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }
    
    public SOSAuthCurrentAccountAnswer getSosAuthCurrentAccountAnswer() {
        return sosAuthCurrentAccountAnswer;
    }

}

