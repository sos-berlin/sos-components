package com.sos.joc.exceptions;

import java.util.Date;

public class JocBadRequestException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-400";
    private Date surveyDate = null;

    public JocBadRequestException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocBadRequestException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocBadRequestException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocBadRequestException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocBadRequestException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocBadRequestException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocBadRequestException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocBadRequestException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
