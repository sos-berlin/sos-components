package com.sos.joc.exceptions;

import java.util.Date;

public class KeyNotExistException extends JobSchedulerBadRequestException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-420";
    private Date surveyDate = null;

    public KeyNotExistException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public KeyNotExistException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public KeyNotExistException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public KeyNotExistException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public KeyNotExistException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public KeyNotExistException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public KeyNotExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public KeyNotExistException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
