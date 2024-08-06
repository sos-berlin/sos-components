package com.sos.joc.logmanagement.exception;

public class SOSUnexpectedMessageException extends SOSLogManagementException {

    private static final long serialVersionUID = 1L;

    public SOSUnexpectedMessageException(String message) {
        super(message);
    }
    
    public SOSUnexpectedMessageException(Throwable cause) {
        super(cause);
    }
    
    public SOSUnexpectedMessageException(String message, Throwable cause) {
        super(message, cause);
    }

}
