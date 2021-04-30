package com.sos.jitl.jobs.exception;

public class SOSJobRequiredArgumentMissingException extends SOSJobArgumentException {

    private static final long serialVersionUID = 1L;

    private String argumentName;

    public SOSJobRequiredArgumentMissingException(String message) {
        super(message);
    }

    public SOSJobRequiredArgumentMissingException(String argumentName, String message) {
        super(message);
        this.argumentName = argumentName;
    }

    public String getArgumentName() {
        return argumentName;
    }
}
