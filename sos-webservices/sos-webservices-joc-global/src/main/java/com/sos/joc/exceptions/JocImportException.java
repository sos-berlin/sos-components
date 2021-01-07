package com.sos.joc.exceptions;

import java.util.Date;

public class JocImportException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-417";
    private Date surveyDate = null;

    public JocImportException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocImportException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocImportException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocImportException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocImportException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocImportException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocImportException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocImportException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
