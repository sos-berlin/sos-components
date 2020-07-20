package com.sos.joc.exceptions;

import java.util.Date;

public class JocSignatureVerificationException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-412";
    private Date surveyDate = null;

    public JocSignatureVerificationException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocSignatureVerificationException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocSignatureVerificationException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocSignatureVerificationException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocSignatureVerificationException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocSignatureVerificationException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocSignatureVerificationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocSignatureVerificationException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
