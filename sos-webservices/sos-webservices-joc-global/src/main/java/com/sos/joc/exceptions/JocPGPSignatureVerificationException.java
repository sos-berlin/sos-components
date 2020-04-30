package com.sos.joc.exceptions;

import java.util.Date;

public class JocPGPSignatureVerificationException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-412";
    private Date surveyDate = null;

    public JocPGPSignatureVerificationException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocPGPSignatureVerificationException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocPGPSignatureVerificationException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocPGPSignatureVerificationException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocPGPSignatureVerificationException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocPGPSignatureVerificationException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocPGPSignatureVerificationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocPGPSignatureVerificationException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
