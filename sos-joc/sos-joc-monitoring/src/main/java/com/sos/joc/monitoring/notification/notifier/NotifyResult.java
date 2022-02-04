package com.sos.joc.monitoring.notification.notifier;

public class NotifyResult {

    private final String message;
    private final StringBuilder sendInfo;
    private final StringBuilder skipCause;

    private NotifyResultError error;

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

    public class NotifyResultError {

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
}
