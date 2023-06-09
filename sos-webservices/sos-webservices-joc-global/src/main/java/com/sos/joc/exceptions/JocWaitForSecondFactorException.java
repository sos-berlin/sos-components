package com.sos.joc.exceptions;

import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;

public class JocWaitForSecondFactorException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-401";
    private SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = null;

    public JocWaitForSecondFactorException() {
    }

    public JocWaitForSecondFactorException(SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer) {
        super(new JocError(ERROR_CODE, sosAuthCurrentAccountAnswer.getMessage()));
        this.sosAuthCurrentAccountAnswer = sosAuthCurrentAccountAnswer;
    }
    
    public JocWaitForSecondFactorException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocWaitForSecondFactorException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocWaitForSecondFactorException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocWaitForSecondFactorException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocWaitForSecondFactorException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocWaitForSecondFactorException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocWaitForSecondFactorException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }
    
    public SOSAuthCurrentAccountAnswer getSosAuthCurrentAccountAnswer() {
        return sosAuthCurrentAccountAnswer;
    }

}

