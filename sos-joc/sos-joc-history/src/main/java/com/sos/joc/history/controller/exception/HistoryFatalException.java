package com.sos.joc.history.controller.exception;

import com.sos.commons.exception.SOSException;

// not a runtime exception
public class HistoryFatalException extends SOSException implements IHistoryProcessingException {

    private static final long serialVersionUID = 1L;

    private final String controllerId;

    public HistoryFatalException(String controllerId, int maxStopProcessingOnErrors, Throwable e) {
        super(String.format(
                "max_stop_processing_on_errors=%s has been exceeded. The History will be stopped and should be started when the problems have been solved.",
                maxStopProcessingOnErrors), e);
        this.controllerId = controllerId;
    }

    @Override
    public String getControllerId() {
        return controllerId;
    }

}
