package com.sos.jitl.jobs.mail;

import java.util.List;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class MailJobArguments extends JobArguments {

    private JobArgument<String> mailSmtpHost = new JobArgument<String>("mail.smtp.host", true);
    private JobArgument<String> mailSmtpPort = new JobArgument<String>("mail.smtp.port", false);
    private JobArgument<String> mailSmtpUser = new JobArgument<String>("mail.smtp.user", false);
    private JobArgument<String> mailSmtpPassword = new JobArgument<String>("mail.smtp.password", false, DisplayMode.MASKED);
    private JobArgument<String> securityProtocol = new JobArgument<String>("security_protocol", false);

    private JobArgument<String> from = new JobArgument<String>("from", false);
    private JobArgument<String> fromName = new JobArgument<String>("from_name", false);
    private JobArgument<String> replyTo = new JobArgument<String>("reply_to", false);
    private JobArgument<String> to = new JobArgument<String>("to", true);
    private JobArgument<String> cc = new JobArgument<String>("cc", false);
    private JobArgument<String> bcc = new JobArgument<String>("bcc", false);
    private JobArgument<String> subject = new JobArgument<String>("subject", false);
    private JobArgument<String> body = new JobArgument<String>("body", false);

    private JobArgument<String> contentType = new JobArgument<String>("content_type", false);
    private JobArgument<String> charset = new JobArgument<String>("charset", false, "utf-8");
    private JobArgument<String> encoding = new JobArgument<String>("encoding", false);

    private JobArgument<String> attachmentCharset = new JobArgument<String>("attachment_charset", false);
    private JobArgument<String> attachmentContentType = new JobArgument<String>("attachment_content_type", false);
    private JobArgument<String> attachmentEncoding = new JobArgument<String>("attachment_encoding", false);
    private JobArgument<Boolean> cleanupAttachment = new JobArgument<Boolean>("cleanup_attachment", false, false);
    private JobArgument<List<String>> attachment = new JobArgument<List<String>>("attachment", false);

    public MailJobArguments() {
        super(new CredentialStoreArguments());
    }

    public String getMailSmtpHost() {
        return mailSmtpHost.getValue();
    }

    public void setMailSmtpHost(String mailSmtpHost) {
        this.mailSmtpHost.setValue(mailSmtpHost);
    }

    public String getMailSmtpPort() {
        return mailSmtpPort.getValue();
    }

    public String getFrom() {
        return from.getValue();
    }

    public String getFromName() {
        return fromName.getValue();
    }

    public String getReplyTo() {
        return replyTo.getValue();
    }

    public String getTo() {
        return to.getValue();
    }

    public String getCc() {
        return cc.getValue();
    }

    public String getBcc() {
        return bcc.getValue();
    }

    public String getSubject() {
        return subject.getValue();
    }

    public String getBody() {
        return body.getValue();
    }

    public String getContentType() {
        return contentType.getValue();
    }

    public String getCharset() {
        return charset.getValue();
    }

    public String getEncoding() {
        return encoding.getValue();
    }

    public String getAttachmentCharset() {
        return attachmentCharset.getValue();
    }

    public String getAttachmentContentType() {
        return attachmentContentType.getValue();
    }

    public String getAttachmentEncoding() {
        return attachmentEncoding.getValue();
    }

    public Boolean getCleanupAttachment() {
        return cleanupAttachment.getValue();
    }

    public String getMailSmtpUser() {
        return mailSmtpUser.getValue();
    }

    public void setMailSmtpUser(String mailSmtpUser) {
        this.mailSmtpUser.setValue(mailSmtpUser);
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword.getValue();
    }

    public void setMailSmtpPassword(String mailSmtpPassword) {
        this.mailSmtpPassword.setValue(mailSmtpPassword);
    }

    public String getSecurityProtocol() {
        return securityProtocol.getValue();
    }

    public List<String> getAttachments() {
        return attachment.getValue();
    }
}
