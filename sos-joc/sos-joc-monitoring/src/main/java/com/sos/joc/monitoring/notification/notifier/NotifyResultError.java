package com.sos.joc.monitoring.notification.notifier;

import java.io.Serializable;

public class NotifyResultError implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String message;
    private final Throwable exception;

    public NotifyResultError(String error) {
        this(error, null);
    }

    public NotifyResultError(String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }
}
