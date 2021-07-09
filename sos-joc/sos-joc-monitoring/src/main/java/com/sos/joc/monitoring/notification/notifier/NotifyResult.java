package com.sos.joc.monitoring.notification.notifier;

public class NotifyResult {

    private final String message;
    private final StringBuilder sendInfo;
    private final String error;

    protected NotifyResult(String message, StringBuilder sendInfo) {
        this(message, sendInfo, null);
    }

    protected NotifyResult(String message, StringBuilder sendInfo, String error) {
        this.message = message;
        this.sendInfo = sendInfo;
        this.error = error;
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

}
