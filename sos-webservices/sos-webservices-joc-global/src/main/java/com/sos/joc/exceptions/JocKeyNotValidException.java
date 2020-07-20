package com.sos.joc.exceptions;

import java.util.Date;

public class JocKeyNotValidException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-411";
    private Date surveyDate = null;

    public JocKeyNotValidException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocKeyNotValidException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocKeyNotValidException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocKeyNotValidException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocKeyNotValidException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocKeyNotValidException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocKeyNotValidException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocKeyNotValidException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
