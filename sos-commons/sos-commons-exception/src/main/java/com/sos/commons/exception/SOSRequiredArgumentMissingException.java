package com.sos.commons.exception;

public class SOSRequiredArgumentMissingException extends SOSException implements ISOSRequiredArgumentMissingException {

    private static final long serialVersionUID = 1L;

    private String argumentName;

    public SOSRequiredArgumentMissingException(String message) {
        super(message);
    }

    public SOSRequiredArgumentMissingException(String argumentName, String message) {
        super(message);
        this.argumentName = argumentName;
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }
}
