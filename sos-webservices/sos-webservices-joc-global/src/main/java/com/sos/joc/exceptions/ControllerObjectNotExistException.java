package com.sos.joc.exceptions;

import java.util.Date;

public class ControllerObjectNotExistException extends JocBadRequestException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-161";
    private Date surveyDate = null;

    public ControllerObjectNotExistException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public ControllerObjectNotExistException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public ControllerObjectNotExistException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public ControllerObjectNotExistException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public ControllerObjectNotExistException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public ControllerObjectNotExistException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public ControllerObjectNotExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public ControllerObjectNotExistException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
