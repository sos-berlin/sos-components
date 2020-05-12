package com.sos.joc.exceptions;

import java.util.Date;

public class JocDeployException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-415";
    private Date surveyDate = null;

    public JocDeployException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocDeployException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocDeployException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocDeployException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocDeployException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocDeployException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocDeployException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocDeployException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
