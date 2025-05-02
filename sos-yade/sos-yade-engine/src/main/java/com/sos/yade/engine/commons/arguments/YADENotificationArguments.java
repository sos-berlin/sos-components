package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class YADENotificationArguments extends ASOSArguments {

    public static final String LABEL = "Notification";

    private YADENotificationMailArguments mail;

    private SOSArgument<Boolean> onSuccess = new SOSArgument<>("OnSuccess", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> onError = new SOSArgument<>("OnError", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> onEmptyFiles = new SOSArgument<>("OnEmptyFiles", false, Boolean.valueOf(false));

    public boolean isEnabled() {
        return (onSuccess.isTrue() || onError.isTrue() || onEmptyFiles.isTrue());
    }

    public boolean isMailEnabled() {
        return mail != null && mail.isEnabled();
    }

    public YADENotificationMailArguments getMail() {
        if (mail == null) {
            mail = new YADENotificationMailArguments();
            mail.applyDefaultIfNullQuietly();
        }
        return mail;
    }

    public SOSArgument<Boolean> getOnSuccess() {
        return onSuccess;
    }

    public SOSArgument<Boolean> getOnError() {
        return onError;
    }

    public SOSArgument<Boolean> getOnEmptyFiles() {
        return onEmptyFiles;
    }

}
