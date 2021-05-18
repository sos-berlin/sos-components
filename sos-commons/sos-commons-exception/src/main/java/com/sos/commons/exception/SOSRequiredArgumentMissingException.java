package com.sos.commons.exception;

public class SOSRequiredArgumentMissingException extends SOSException {

    private static final long serialVersionUID = 1L;

    private String argumentName;

    public SOSRequiredArgumentMissingException(String message) {
        super(message);
    }

    public SOSRequiredArgumentMissingException(String argumentName, String message) {
        super(message);
        this.argumentName = argumentName;
    }

    public String getArgumentName() {
        return argumentName;
    }
}
