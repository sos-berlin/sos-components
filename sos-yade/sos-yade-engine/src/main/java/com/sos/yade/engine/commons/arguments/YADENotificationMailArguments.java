package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class YADENotificationMailArguments extends ASOSArguments {

    private SOSArgument<String> headerFrom = new SOSArgument<>("From", false, "yade@sos-berlin.com");
    private SOSArgument<List<String>> headerTo = new SOSArgument<>("To", false);
    private SOSArgument<List<String>> headerCC = new SOSArgument<>("CC", false);
    private SOSArgument<List<String>> headerBCC = new SOSArgument<>("BCC", false);
    private SOSArgument<String> headerSubject = new SOSArgument<>("Subject", false);

    private SOSArgument<String> body = new SOSArgument<>("Body", false);
    private SOSArgument<String> contentType = new SOSArgument<>("ContentType", false, "text/plain");
    private SOSArgument<String> encoding = new SOSArgument<>("Encoding", false, "7bit");
    private SOSArgument<List<Path>> attachment = new SOSArgument<>("Attachment", false);

    public boolean isEnabled() {
        return !headerTo.isEmpty();
    }

    public String getNewLine() {
        return contentType.getValue().toLowerCase().contains("html") ? "<br/>" : "\n";
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

    public SOSArgument<String> getHeaderSubject() {
        return headerSubject;
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
