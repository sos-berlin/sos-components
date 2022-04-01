package com.sos.joc.exceptions;

import java.util.Date;

public class JocGitException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-450";
    private Date surveyDate = null;

    public JocGitException() {
    }

    public Date getSurveyDate() {
        return surveyDate;
    }

    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    public JocGitException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocGitException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocGitException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocGitException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocGitException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocGitException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocGitException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
