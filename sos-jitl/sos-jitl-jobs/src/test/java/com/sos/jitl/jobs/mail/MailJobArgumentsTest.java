package com.sos.jitl.jobs.mail;

import java.util.ArrayList;

public class MailJobArgumentsTest extends MailJobArguments {

    public void setMailSmtpHost(String mailSmtpHost) {
        this.mailSmtpHost.setValue(mailSmtpHost);
    }

    public void setMailSmtpPort(String mailSmtpPort) {
        this.mailSmtpPort.setValue(mailSmtpPort);
    }

    public void setFrom(String from) {
        this.from.setValue(from);
    }

    public void setFromName(String fromName) {
        this.fromName.setValue(fromName);
    }

    public void setReplyTo(String replyTo) {
        this.replyTo.setValue(replyTo);
    }

    public void setTo(String to) {
        this.to.setValue(to);
    }

    public void setCc(String cc) {
        this.cc.setValue(cc);
    }

    public void setBcc(String bcc) {
        this.bcc.setValue(bcc);
    }

    public void setSubject(String subject) {
        this.subject.setValue(subject);
    }

    public void setBody(String body) {
        this.body.setValue(body);
    }

    public void setContentType(String contentType) {
        this.contentType.setValue(contentType);
    }

    public void setCharset(String charset) {
        this.charset.setValue(charset);
    }

    public void setEncoding(String encoding) {
        this.encoding.setValue(encoding);
    }

    public void setAttachmentCharset(String attachmentCharset) {
        this.attachmentCharset.setValue(attachmentCharset);
    }

    public void setAttachmentContentType(String attachmentContentType) {
        this.attachmentContentType.setValue(attachmentContentType);
    }

    public void setAttachmentEncoding(String attachmentEncoding) {
        this.attachmentEncoding.setValue(attachmentEncoding);
    }

    public void setCleanupAttachment(Boolean cleanupAttachment) {
        this.cleanupAttachment.setValue(cleanupAttachment);
    }

    public void setMailSmtpUser(String mailSmtpUser) {
        this.mailSmtpUser.setValue(mailSmtpUser);
    }

    public void setMailSmtpPassword(String mailSmtpPassword) {
        this.mailSmtpPassword.setValue(mailSmtpPassword);
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol.setValue(securityProtocol);
    }

    public void setAttachments(String attachments) {
        this.attachment.setValue(new ArrayList<String>());
        String[] s = attachments.split(";");
        for (String attachment : s) {
            this.attachment.getValue().add(attachment);
        }
    }
}
