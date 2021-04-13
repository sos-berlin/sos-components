package com.sos.jitl.jobs.exception;

import com.sos.commons.exception.SOSException;

import js7.base.problem.Problem;

public class JobProblemException extends SOSException {

    private static final long serialVersionUID = 1L;

    private String message = null;

    public JobProblemException(Problem problem) {
        if (problem == null) {
            message = "unknown problem";
        } else {
            if (problem.codeOrNull() == null) {
                message = problem.message();
            } else {
                message = String.format("[%s]%s", problem.codeOrNull(), problem.messageWithCause());
            }
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
