package com.sos.joc.exceptions;

import java.util.Date;

public class JocKeyNotParseableException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-416";
    private Date surveyDate = null;

    public JocKeyNotParseableException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocKeyNotParseableException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocKeyNotParseableException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocKeyNotParseableException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocKeyNotParseableException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocKeyNotParseableException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocKeyNotParseableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocKeyNotParseableException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
