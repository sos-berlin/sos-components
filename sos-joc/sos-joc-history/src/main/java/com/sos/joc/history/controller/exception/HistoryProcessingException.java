package com.sos.joc.history.controller.exception;

import java.util.Date;

public class HistoryProcessingException extends RuntimeException implements IHistoryProcessingException {

    private static final long serialVersionUID = 1L;

    private final String controllerId;

    public HistoryProcessingException(String controllerId, Throwable e) {
        this(controllerId, e, -1, null, -1);
    }

    public HistoryProcessingException(String controllerId, Throwable e, long waitIntervalStopOnErrors, Date lastActivityStart, long errorCounter) {
        super(getMessage(e, waitIntervalStopOnErrors, errorCounter), e);
        this.controllerId = controllerId;
    }

    private static String getMessage(Throwable e, long waitIntervalStopOnErrors, long errorCounter) {
        if (waitIntervalStopOnErrors < 0) {
            return e.toString();
        }
        return String.format("wait_interval_stop_processing_on_errors=%ss has been exceeded(%s errors occured)", waitIntervalStopOnErrors,
                errorCounter);
    }

    @Override
    public String getControllerId() {
        return controllerId;
    }

}
