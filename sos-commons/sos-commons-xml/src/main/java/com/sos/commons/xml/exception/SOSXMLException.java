package com.sos.commons.xml.exception;

import com.sos.commons.exception.SOSException;

public class SOSXMLException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSXMLException(String message) {
        super(message);
    }
    
    public SOSXMLException(String message, Throwable cause) {
        super(message, cause);
    }

}
