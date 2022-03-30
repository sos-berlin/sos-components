package com.sos.joc.history.controller.exception;

import com.sos.commons.exception.SOSException;

import js7.base.problem.Problem;

public class FatEventProblemException extends SOSException {

    private static final long serialVersionUID = 1L;

    private String message = null;

    public FatEventProblemException(String message) {
        this.message = message;
    }

    public FatEventProblemException(Problem problem) {
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
