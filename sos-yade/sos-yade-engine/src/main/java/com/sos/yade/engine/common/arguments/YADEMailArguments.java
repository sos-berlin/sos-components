package com.sos.yade.engine.common.arguments;

import com.sos.commons.util.common.SOSArgument;

public class YADEMailArguments {

    // TODO - only if standalone .... ?
    private SOSArgument<Boolean> mailOnSuccess = new SOSArgument<>("mail_on_success", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnError = new SOSArgument<>("mail_on_error", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnEmptyFiles = new SOSArgument<>("mail_on_empty_files", false, Boolean.valueOf(false));
    
    public SOSArgument<Boolean> getMailOnSuccess() {
        return mailOnSuccess;
    }

    public SOSArgument<Boolean> getMailOnError() {
        return mailOnError;
    }

    public SOSArgument<Boolean> getMailOnEmptyFiles() {
        return mailOnEmptyFiles;
    }

}
