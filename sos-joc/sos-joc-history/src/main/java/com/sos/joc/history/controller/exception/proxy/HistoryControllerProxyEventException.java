package com.sos.joc.history.controller.exception.proxy;

import com.sos.commons.exception.SOSException;
import com.sos.joc.history.controller.exception.IHistoryProcessingException;

import js7.base.problem.Problem;

public class HistoryControllerProxyEventException extends SOSException implements IHistoryProcessingException {

    private static final long serialVersionUID = 1L;

    private final String controllerId;

    public HistoryControllerProxyEventException(String controllerId, Problem problem) {
        this(controllerId, getProblemMessage(problem));
    }

    public HistoryControllerProxyEventException(String controllerId, String message) {
        super(message);
        this.controllerId = controllerId;
    }

    private static String getProblemMessage(Problem problem) {
        if (problem == null) {
            return "unknown problem";
        }
        return problem.codeOrNull() == null ? problem.message() : String.format("[%s]%s", problem.codeOrNull(), problem.messageWithCause());
    }

    @Override
    public String getControllerId() {
        return controllerId;
    }

}
