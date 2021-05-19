package com.sos.jitl.jobs.exception;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;

public class SOSJobRequiredArgumentMissingException extends SOSJobArgumentException implements ISOSRequiredArgumentMissingException {

    private static final long serialVersionUID = 1L;

    private String argumentName;

    public SOSJobRequiredArgumentMissingException(String message) {
        super(message);
    }

    public SOSJobRequiredArgumentMissingException(String argumentName, String message) {
        super(message);
        this.argumentName = argumentName;
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }
}
