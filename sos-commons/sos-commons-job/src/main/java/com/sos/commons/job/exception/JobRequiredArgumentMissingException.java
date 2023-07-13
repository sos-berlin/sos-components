package com.sos.commons.job.exception;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;

public class JobRequiredArgumentMissingException extends JobArgumentException implements ISOSRequiredArgumentMissingException {

    private static final long serialVersionUID = 1L;

    private String argumentName;

    public JobRequiredArgumentMissingException(String message) {
        super(message);
    }

    public JobRequiredArgumentMissingException(String argumentName, String message) {
        super(message);
        this.argumentName = argumentName;
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }
}
