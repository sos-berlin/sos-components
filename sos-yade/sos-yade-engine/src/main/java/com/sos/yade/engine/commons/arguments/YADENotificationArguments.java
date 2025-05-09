package com.sos.yade.engine.commons.arguments;

public class YADENotificationArguments {

    public static final String LABEL = "Notification";
    public static final String LABEL_ON_SUCCESS = "OnSuccess";
    public static final String LABEL_ON_ERROR = "OnError";
    public static final String LABEL_ON_EMPTY_FILES = "OnEmptyFiles";

    private YADENotificationMailServerArguments mailServer;

    private YADENotificationMailArguments mailOnSuccess;
    private YADENotificationMailArguments mailOnError;
    private YADENotificationMailArguments mailOnEmptyFiles;

    public boolean isEnabled() {
        return mailServer != null && isTriggerEnabled();
    }

    public boolean isTriggerEnabled() {
        return mailOnSuccess != null || mailOnError != null || mailOnEmptyFiles != null;
    }

    public void setMailServer(YADENotificationMailServerArguments val) {
        mailServer = val;
    }

    public YADENotificationMailServerArguments getMailServer() {
        if (mailServer == null) {
            mailServer = new YADENotificationMailServerArguments();
            mailServer.applyDefaultIfNullQuietly();
        }
        return mailServer;
    }

    public YADENotificationMailArguments getMailOnSuccess() {
        return mailOnSuccess;
    }

    public void setMailOnSuccess(YADENotificationMailArguments val) {
        mailOnSuccess = val;
    }

    public YADENotificationMailArguments getMailOnError() {
        return mailOnError;
    }

    public void setMailOnError(YADENotificationMailArguments val) {
        mailOnError = val;
    }

    public YADENotificationMailArguments getMailOnEmptyFiles() {
        return mailOnEmptyFiles;
    }

    public void setMailOnEmptyFiles(YADENotificationMailArguments val) {
        mailOnEmptyFiles = val;
    }

}
