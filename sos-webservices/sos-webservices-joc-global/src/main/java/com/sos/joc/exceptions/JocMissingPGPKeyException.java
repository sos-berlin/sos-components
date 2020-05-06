package com.sos.joc.exceptions;

import java.util.Date;

public class JocMissingPGPKeyException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-413";
    private Date surveyDate = null;

    public JocMissingPGPKeyException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocMissingPGPKeyException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocMissingPGPKeyException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocMissingPGPKeyException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocMissingPGPKeyException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocMissingPGPKeyException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocMissingPGPKeyException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocMissingPGPKeyException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
