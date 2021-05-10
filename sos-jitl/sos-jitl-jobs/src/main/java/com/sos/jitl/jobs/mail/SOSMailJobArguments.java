package com.sos.jitl.jobs.mail;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArgument.DisplayMode;
import com.sos.jitl.jobs.common.JobArguments;

public class SOSMailJobArguments extends JobArguments {

    private JobArgument<String> mailSmtpHost = new JobArgument<String>("mail.smtp.host", true);
    private JobArgument<Integer> mailSmtpPort = new JobArgument<Integer>("mail.smtp.port", false, 25);
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
    private JobArgument<Boolean> cleanupAttachment = new JobArgument<Boolean>("cleanup_attachment", false,false);
    private JobArgument<String> mailSmtpUser = new JobArgument<String>("mail.smtp.user", false);
    private JobArgument<String> mailSmtpPassword = new JobArgument<String>("mail.smtp.password", false, DisplayMode.MASKED);
    private JobArgument<String> securityProtocol = new JobArgument<String>("security_protocol", false);
    private JobArgument<String[]> attachments = new JobArgument<String[]>("attachments", false);

    private JobArgument<String> credentialStoreFile = new JobArgument<String>("credential_store_file", false);
    private JobArgument<String> credentialStoreKeyFile = new JobArgument<String>("credential_store_key_file", false);
    private JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password", false);
    private JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path", false);

    public String getMailSmtpHost() {
        return mailSmtpHost.getValue();
    }

    public void setMailSmtpHost(String mailSmtpHost) {
        this.mailSmtpHost.setValue(mailSmtpHost);
    }

    public Integer getMailSmtpPort() {
        return mailSmtpPort.getValue();
    }

    public void setMailSmtpPort(Integer mailSmtpPort) {
        this.mailSmtpPort.setValue(mailSmtpPort);
    }

    public String getFrom() {
        return from.getValue();
    }

    public void setFrom(String from) {
        this.from.setValue(from);
    }

    public String getFromName() {
        return fromName.getValue();
    }

    public void setFromName(String fromName) {
        this.fromName.setValue(fromName);
    }

    public String getReplyTo() {
        return replyTo.getValue();
    }

    public void setReplyTo(String replyTo) {
        this.replyTo.setValue(replyTo);
    }

    public String getTo() {
        return to.getValue();
    }

    public void setTo(String to) {
        this.to.setValue(to);
    }

    public String getCc() {
        return cc.getValue();
    }

    public void setCc(String cc) {
        this.cc.setValue(cc);
    }

    public String getBcc() {
        return bcc.getValue();
    }

    public void setBcc(String bcc) {
        this.bcc.setValue(bcc);
    }

    public String getSubject() {
        return subject.getValue();
    }

    public void setSubject(String subject) {
        this.subject.setValue(subject);
    }

    public String getBody() {
        return body.getValue();
    }

    public void setBody(String body) {
        this.body.setValue(body);
    }

    public String getContentType() {
        return contentType.getValue();
    }

    public void setContentType(String contentType) {
        this.contentType.setValue(contentType);
    }

    public String getCharset() {
        return charset.getValue();
    }

    public void setCharset(String charset) {
        this.charset.setValue(charset);
    }

    public String getEncoding() {
        return encoding.getValue();
    }

    public void setEncoding(String encoding) {
        this.encoding.setValue(encoding);
    }

    public String getAttachmentCharset() {
        return attachmentCharset.getValue();
    }

    public void setAttachmentCharset(String attachmentCharset) {
        this.attachmentCharset.setValue(attachmentCharset);
    }

    public String getAttachmentContentType() {
        return attachmentContentType.getValue();
    }

    public void setAttachmentContentType(String attachmentContentType) {
        this.attachmentContentType.setValue(attachmentContentType);
    }

    public String getAttachmentEncoding() {
        return attachmentEncoding.getValue();
    }

    public void setAttachmentEncoding(String attachmentEncoding) {
        this.attachmentEncoding.setValue(attachmentEncoding);
    }

    public Boolean getCleanupAttachment() {
        return cleanupAttachment.getValue();
    }

    public void setCleanupAttachment(Boolean cleanupAttachment) {
        this.cleanupAttachment.setValue(cleanupAttachment);
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

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol.setValue(securityProtocol);
    }

    public String[] getAttachments() {
        return attachments.getValue();
    }

    public void setAttachments(String attachments) {
        this.attachments.setValue(attachments.split(";"));
    }

    public String getCredentialStoreFile() {
        return credentialStoreFile.getValue();
    }

    public void setCredentialStoreFile(String credentialStoreFile) {
        this.credentialStoreFile.setValue(credentialStoreFile);
    }

    public String getCredentialStoreKeyFile() {
        return credentialStoreKeyFile.getValue();
    }

    public void setCredentialStoreKeyFile(String credentialStoreKeyFile) {
        this.credentialStoreKeyFile.setValue(credentialStoreKeyFile);
    }

    public String getCredentialStorePassword() {
        return credentialStorePassword.getValue();
    }

    public void setCredentialStorePassword(String credentialStorePassword) {
        this.credentialStorePassword.setValue(credentialStorePassword);
    }

    public String getCredentialStoreEntryPath() {
        return credentialStoreEntryPath.getValue();
    }

    public void setCredentialStoreEntryPath(String credentialStoreEntryPath) {
        this.credentialStoreEntryPath.setValue(credentialStoreEntryPath);
    }

}
