package com.sos.joc.logmanagement.exception;

public class SOSLogManagementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SOSLogManagementException(String message) {
        super(message);
    }
    
    public SOSLogManagementException(Throwable cause) {
        super(cause);
    }
    
    public SOSLogManagementException(String message, Throwable cause) {
        super(message, cause);
    }

}
