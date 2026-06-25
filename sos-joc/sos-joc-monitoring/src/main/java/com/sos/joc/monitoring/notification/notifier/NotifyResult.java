package com.sos.joc.monitoring.notification.notifier;

import java.io.Serializable;

public class NotifyResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String message;
    private final StringBuilder sendInfo;
    private final StringBuilder skipCause;

    private NotifyResultError error;

    public NotifyResult(String message, Throwable exception) {
        this(message, null, null);
        error = new NotifyResultError(exception.getMessage(), exception);
    }

    protected NotifyResult(String message, StringBuilder sendInfo) {
        this(message, sendInfo, null);
    }

    protected NotifyResult(String message, StringBuilder sendInfo, StringBuilder skipCause) {
        this.message = message;
        this.sendInfo = sendInfo;
        this.skipCause = skipCause;
    }

    protected void setError(String msg) {
        setError(msg, null);
    }

    protected void setError(String msg, Throwable e) {
        error = new NotifyResultError(msg, e);
    }

    public String getMessage() {
        return message;
    }

    public StringBuilder getSendInfo() {
        return sendInfo;
    }

    public NotifyResultError getError() {
        return error;
    }

    public StringBuilder getSkipCause() {
        return skipCause;
    }

}
