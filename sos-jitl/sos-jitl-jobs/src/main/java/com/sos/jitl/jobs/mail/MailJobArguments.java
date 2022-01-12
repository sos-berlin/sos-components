package com.sos.jitl.jobs.mail;

import java.util.List;

import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class MailJobArguments extends JobArguments {

    protected JobArgument<String> mailSmtpHost = new JobArgument<String>("mail.smtp.host", true);
    // TODO release 2.2.1 changed from Integer to String - check and change back ...
    protected JobArgument<String> mailSmtpPort = new JobArgument<String>("mail.smtp.port", false);
    protected JobArgument<String> from = new JobArgument<String>("from", false);
    protected JobArgument<String> fromName = new JobArgument<String>("from_name", false);
    protected JobArgument<String> replyTo = new JobArgument<String>("reply_to", false);
    protected JobArgument<String> to = new JobArgument<String>("to", true);
    protected JobArgument<String> cc = new JobArgument<String>("cc", false);
    protected JobArgument<String> bcc = new JobArgument<String>("bcc", false);
    protected JobArgument<String> subject = new JobArgument<String>("subject", false);
    protected JobArgument<String> body = new JobArgument<String>("body", false);
    protected JobArgument<String> contentType = new JobArgument<String>("content_type", false);

    protected JobArgument<String> charset = new JobArgument<String>("charset", false, "utf-8");
    protected JobArgument<String> encoding = new JobArgument<String>("encoding", false);
    protected JobArgument<String> attachmentCharset = new JobArgument<String>("attachment_charset", false);
    protected JobArgument<String> attachmentContentType = new JobArgument<String>("attachment_content_type", false);
    protected JobArgument<String> attachmentEncoding = new JobArgument<String>("attachment_encoding", false);
    protected JobArgument<Boolean> cleanupAttachment = new JobArgument<Boolean>("cleanup_attachment", false, false);
    protected JobArgument<String> mailSmtpUser = new JobArgument<String>("mail.smtp.user", false);
    protected JobArgument<String> mailSmtpPassword = new JobArgument<String>("mail.smtp.password", false, DisplayMode.MASKED);
    protected JobArgument<String> securityProtocol = new JobArgument<String>("security_protocol", false);
    protected JobArgument<List<String>> attachment = new JobArgument<List<String>>("attachment", false);

    protected JobArgument<String> credentialStoreFile = new JobArgument<String>("credential_store_file", false);
    protected JobArgument<String> credentialStoreKeyFile = new JobArgument<String>("credential_store_key_file", false);
    protected JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password", false);
    protected JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path", false);

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

    public String getCredentialStoreFile() {
        return credentialStoreFile.getValue();
    }

    public String getCredentialStoreKeyFile() {
        return credentialStoreKeyFile.getValue();
    }

    public String getCredentialStorePassword() {
        return credentialStorePassword.getValue();
    }

    public String getCredentialStoreEntryPath() {
        return credentialStoreEntryPath.getValue();
    }

}
