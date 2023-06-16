package com.sos.joc.exceptions;

public class JocException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private JocError error;
    
    public JocException() {
        super();
    }

    public JocException(JocError error) {
        super(error.toString());
        this.error = error;
    }
    
    public JocException(Throwable cause) {
        super(cause);
        this.error = new JocError();
        error.setMessage(cause.getMessage());
    }

    public JocException(JocError error, Throwable cause) {
        super(error.toString(), cause);
        this.error = error;
    }

    public JocException(JocError error, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(error.toString(), cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    public JocError getError() {
        return error;
    }

    public void setError(JocError error) {
        this.error = error;
    }
    
    public static JocError updateJocErrorCode(JocError jocError, String code) {
        if (code != null) {
            jocError.setCode(code);
        }
        return jocError;
    }
    
    public void setErrorMessage(String message) {
        if (this.error == null) {
            this.error = new JocError(); 
        }
        this.error.setMessage(message);
    }
    
    public void addErrorMetaInfo(JocError error) {
        if (this.error == null) {
            this.error = new JocError(); 
        }
        this.error.addMetaInfoOnTop(error.getMetaInfo());
        error.getMetaInfo().clear();
        this.error.setApiCall(error.getApiCall());
    }

}
