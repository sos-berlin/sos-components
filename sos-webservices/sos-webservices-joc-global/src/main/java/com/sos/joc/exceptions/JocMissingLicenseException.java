package com.sos.joc.exceptions;


public class JocMissingLicenseException extends JocException {
    
    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "JOC-444";

    public JocMissingLicenseException() {
        super(new JocError(ERROR_CODE, "missing comment"));
    }

    public JocMissingLicenseException(Throwable cause) {
        super(new JocError(ERROR_CODE, cause.getMessage()), cause);
    }

    public JocMissingLicenseException(String message) {
        super(new JocError(ERROR_CODE, message));
    }
    
    public JocMissingLicenseException(JocError error) {
        super(updateJocErrorCode(error, ERROR_CODE));
    }

    public JocMissingLicenseException(String message, Throwable cause) {
        super(new JocError(ERROR_CODE, message), cause);
    }

    public JocMissingLicenseException(JocError error, Throwable cause) {
        super(updateJocErrorCode(error, ERROR_CODE), cause);
    }

    public JocMissingLicenseException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(new JocError(ERROR_CODE, message), cause, enableSuppression, writableStackTrace);
    }
    
    public JocMissingLicenseException(JocError error, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(updateJocErrorCode(error, ERROR_CODE), cause, enableSuppression, writableStackTrace);
    }

}
