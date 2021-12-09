package com.sos.joc.monitoring.notification.notifier;

public class NotifyResult {

    private final String message;
    private final StringBuilder sendInfo;
    private final String error;
    private final StringBuilder skipCause;

    protected NotifyResult(String message, StringBuilder sendInfo) {
        this(message, sendInfo, null, null);
    }

    protected NotifyResult(String message, StringBuilder sendInfo, StringBuilder skipCause) {
        this(message, sendInfo, null, skipCause);
    }

    protected NotifyResult(String message, StringBuilder sendInfo, String error) {
        this(message, sendInfo, error, null);
    }

    protected NotifyResult(String message, StringBuilder sendInfo, String error, StringBuilder skipCause) {
        this.message = message;
        this.sendInfo = sendInfo;
        this.error = error;
        this.skipCause = skipCause;
    }

    public String getMessage() {
        return message;
    }

    public StringBuilder getSendInfo() {
        return sendInfo;
    }

    public String getError() {
        return error;
    }

    public StringBuilder getSkipCause() {
        return skipCause;
    }
}
