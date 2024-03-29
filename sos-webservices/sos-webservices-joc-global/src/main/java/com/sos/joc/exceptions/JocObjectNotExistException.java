package com.sos.joc.exceptions;

import java.util.Date;

public class JocObjectNotExistException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-002";
    private Date surveyDate = null;

    public JocObjectNotExistException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocObjectNotExistException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocObjectNotExistException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocObjectNotExistException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocObjectNotExistException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocObjectNotExistException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocObjectNotExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocObjectNotExistException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
