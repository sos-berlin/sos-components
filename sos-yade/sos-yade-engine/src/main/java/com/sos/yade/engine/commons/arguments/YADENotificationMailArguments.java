package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class YADENotificationMailArguments extends ASOSArguments {

    private SOSArgument<String> hostname = new SOSArgument<>("Hostname", false);
    private SOSArgument<Integer> port = new SOSArgument<>("Port", false, Integer.valueOf(25));
    private SOSArgument<String> account = new SOSArgument<>("Account", false);
    private SOSArgument<String> password = new SOSArgument<>("Password", false, DisplayMode.MASKED);

    private SOSArgument<String> queueDirectory = new SOSArgument<>("QueueDirectory", false);

    private SOSArgument<String> headerFrom = new SOSArgument<>("From", false);
    private SOSArgument<List<String>> headerTo = new SOSArgument<>("To", false);
    private SOSArgument<List<String>> headerCC = new SOSArgument<>("CC", false);
    private SOSArgument<List<String>> headerBCC = new SOSArgument<>("BCC", false);

    private SOSArgument<String> body = new SOSArgument<>("Body", false);
    private SOSArgument<String> contentType = new SOSArgument<>("ContentType", false, "text/plain");
    private SOSArgument<String> encoding = new SOSArgument<>("Encoding", false, "7bit");
    private SOSArgument<List<Path>> attachment = new SOSArgument<>("Attachment", false);

    public boolean isEnabled() {
        return !hostname.isEmpty();
    }

    public SOSArgument<String> getHostname() {
        return hostname;
    }

    public SOSArgument<Integer> getPort() {
        return port;
    }

    public SOSArgument<String> getAccount() {
        return account;
    }

    public SOSArgument<String> getPassword() {
        return password;
    }

    public SOSArgument<String> getQueueDirectory() {
        return queueDirectory;
    }

    public SOSArgument<String> getHeaderFrom() {
        return headerFrom;
    }

    public SOSArgument<List<String>> getHeaderTo() {
        return headerTo;
    }

    public SOSArgument<List<String>> getHeaderCC() {
        return headerCC;
    }

    public SOSArgument<List<String>> getHeaderBCC() {
        return headerBCC;
    }

    public SOSArgument<String> getBody() {
        return body;
    }

    public SOSArgument<String> getContentType() {
        return contentType;
    }

    public SOSArgument<String> getEncoding() {
        return encoding;
    }

    public SOSArgument<List<Path>> getAttachment() {
        return attachment;
    }

}
