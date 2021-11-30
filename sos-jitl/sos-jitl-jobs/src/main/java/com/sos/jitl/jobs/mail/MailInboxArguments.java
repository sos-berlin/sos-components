package com.sos.jitl.jobs.mail;

import java.util.Collections;
import java.util.List;

import com.sos.commons.mail.SOSMailReceiver;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class MailInboxArguments extends JobArguments {

    public enum ActionAfterProcess {
        none, move, copy, delete, markAsRead, mark_as_read
    }
    
    public enum ActionProcess {
        none, dump, dumpAttachments, dump_attachments
    }
    
    /* imap arguments https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html */
    private JobArgument<String> mailImapHost = new JobArgument<String>("mail.imap.host", false);
    private JobArgument<Integer> mailImapPort = new JobArgument<Integer>("mail.imap.port", false); //143, 993 ssl
    private JobArgument<String> mailImapUser = new JobArgument<String>("mail.imap.user", false);
    private JobArgument<String> mailImapPassword = new JobArgument<String>("mail.imap.password", false, "", DisplayMode.MASKED, null);
    private JobArgument<Boolean> mailImapSSL = new JobArgument<Boolean>("mail.imap.ssl.enable", false, false);
    
    /* pop3 arguments https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html */
    private JobArgument<String> mailPop3Host = new JobArgument<String>("mail.pop3.host", false);
    private JobArgument<Integer> mailPop3Port = new JobArgument<Integer>("mail.pop3.port", false); //110, 995 ssl
    private JobArgument<String> mailPop3User = new JobArgument<String>("mail.pop3.user", false);
    private JobArgument<String> mailPop3Password = new JobArgument<String>("mail.pop3.password", false, "", DisplayMode.MASKED, null);
    private JobArgument<Boolean> mailPop3SSL = new JobArgument<Boolean>("mail.pop3.ssl.enable", false, false);
    
    /* mail store arguments */
    private JobArgument<SOSMailReceiver.Protocol> mailStoreProtocol = new JobArgument<SOSMailReceiver.Protocol>("mail.store.protocol", true);
    //private JobArgument<String> mailPassword = new JobArgument<String>("mail_password", false, "", DisplayMode.MASKED, null);
    
    /* mail messages arguments */
    private JobArgument<List<String>> mailMessageFolder = new JobArgument<List<String>>("mail_source_folders", false, Collections.singletonList("INBOX"), null);
    
    private JobArgument<Integer> maxMailsToProcess = new JobArgument<Integer>("max_processed_mails", false, 1000);
    private JobArgument<String> minMailAge = new JobArgument<String>("min_mail_age", false, "0");// seconds or hh:mm:ss
    private JobArgument<String> mailBodyPattern = new JobArgument<String>("mail_body_pattern", false, "");
    private JobArgument<String> mailSubjectFilter = new JobArgument<String>("mail_subject_filter", false, "");
    private JobArgument<String> mailSubjectPattern = new JobArgument<String>("mail_subject_pattern", false, "");
    private JobArgument<String> mailFromFilter = new JobArgument<String>("mail_from_filter", false, "");
    
    private JobArgument<List<ActionProcess>> mailAction = new JobArgument<List<ActionProcess>>("mail_action", false, Collections.singletonList(
            ActionProcess.none));
    private JobArgument<ActionAfterProcess> afterProcessMail = new JobArgument<ActionAfterProcess>("mail_post_action", false,
            ActionAfterProcess.none);
    private JobArgument<String> afterProcessMailDirectoryName = new JobArgument<String>("mail_target_folder", false, "");
    private JobArgument<String> attachmentDirectoryName = new JobArgument<String>("mail_attachments_directory", false, "");
    //private JobArgument<Boolean> copyMailToFile = new JobArgument<Boolean>("dump", false, false);
    private JobArgument<Boolean> saveBodyAsAttachment = new JobArgument<Boolean>("body_as_attachment", false, false);
    //private JobArgument<Boolean> deleteMail = new JobArgument<Boolean>("delete_mail", false, false);
    private JobArgument<Boolean> mailOnlyUnseen = new JobArgument<Boolean>("only_unread_mails", false, true);
    private JobArgument<String> attachmentFileNamePattern = new JobArgument<String>("attachment_file_name_pattern", false, "");
    private JobArgument<String> mailDirectoryName = new JobArgument<String>("mail_file_directory", false, "");
    //private JobArgument<Boolean> copyAttachmentsToFile = new JobArgument<Boolean>("dump_attachments", false, false);
    
     
    /* credential store arguments */
    private JobArgument<String> credentialStoreFile = new JobArgument<String>("credential_store_file", false);
    private JobArgument<String> credentialStoreKeyFile = new JobArgument<String>("credential_store_key_file", false);
    private JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password", false);
    private JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path", false);
    
    public List<JobArgument<?>> createRequiredMailArguments(SOSMailReceiver.Protocol protocol) {
        switch (protocol) {
        case imap:
            return createRequiredIMAPArguments();
        case pop3:
            return createRequiredPOP3Arguments();
        }
        return Collections.emptyList();
    }
    
    public List<JobArgument<?>> createRequiredMailArguments() {
        return createRequiredMailArguments(mailStoreProtocol.getValue());
    }
    
    public void setDefaultMailPort(SOSMailReceiver.Protocol protocol) {
        switch (protocol) {
        case imap:
            if (Boolean.TRUE == mailImapSSL.getValue()) {
                mailImapPort.setDefaultValue(993);
            }
            break;
        case pop3:
            if (Boolean.TRUE == mailPop3SSL.getValue()) {
                mailPop3Port.setDefaultValue(995);
            }
            break;
        }
    }
    
    public void setDefaultMailPort() {
        switch (mailStoreProtocol.getValue()) {
        case imap:
            if (Boolean.TRUE == mailImapSSL.getValue()) {
                mailImapPort.setDefaultValue(993);
            } else {
                mailImapPort.setDefaultValue(143); 
            }
            break;
        case pop3:
            if (Boolean.TRUE == mailPop3SSL.getValue()) {
                mailPop3Port.setDefaultValue(995);
            } else {
                mailPop3Port.setDefaultValue(110); 
            }
            break;
        }
    }
    
    private List<JobArgument<?>> createRequiredIMAPArguments() { 
        mailImapHost.setRequired(true);
        return Collections.singletonList(mailImapHost);
    }
    
    private List<JobArgument<?>> createRequiredPOP3Arguments() { 
        mailPop3Host.setRequired(true);
        return Collections.singletonList(mailPop3Host);
    }
    
    public JobArgument<String> getMailHost() {
        if (mailStoreProtocol.getValue().equals(SOSMailReceiver.Protocol.imap)) {
            return mailImapHost;
        } else {
            return mailPop3Host;
        }
    }
    
    public JobArgument<Integer> getMailPort() {
        if (mailStoreProtocol.getValue().equals(SOSMailReceiver.Protocol.imap)) {
            return mailImapPort;
        } else {
            return mailPop3Port;
        }
    }

    public JobArgument<String> getMailUser() {
        if (mailStoreProtocol.getValue().equals(SOSMailReceiver.Protocol.imap)) {
            return mailImapUser;
        } else {
            return mailPop3User;
        }
    }
    
    public JobArgument<Boolean> getMailSSL() {
        if (mailStoreProtocol.getValue().equals(SOSMailReceiver.Protocol.imap)) {
            return mailImapSSL;
        } else {
            return mailPop3SSL;
        }
    }
    
    public JobArgument<SOSMailReceiver.Protocol> getMailProtocol() {
        return mailStoreProtocol;
    }

    public JobArgument<String> getMailPassword() {
        if (mailStoreProtocol.getValue().equals(SOSMailReceiver.Protocol.imap)) {
            return mailImapPassword;
        } else {
            return mailPop3Password;
        }
    }
    
    public JobArgument<String> getCredentialStoreFile() {
        return credentialStoreFile;
    }
    
    public JobArgument<String> getCredentialStoreKeyFile() {
        return credentialStoreKeyFile;
    }
    
    public JobArgument<String> getCredentialStorePassword() {
        return credentialStorePassword;
    }
    
    public JobArgument<String> getCredentialStoreEntryPath() {
        return credentialStoreEntryPath;
    }
    
    /* getter for mail messages arguments */
    public JobArgument<Integer> getMaxMailsToProcess() {
        return maxMailsToProcess;
    }
    
    public JobArgument<List<ActionProcess>> getAction() {
        return mailAction;
    }
    
    public JobArgument<ActionAfterProcess> getAfterProcessMail() {
        return afterProcessMail;
    }
    
    public JobArgument<String> getAfterProcessMailDirectoryName() {
        return afterProcessMailDirectoryName;
    }
    
    public JobArgument<String> getAttachmentDirectoryName() {
        return attachmentDirectoryName;
    }
    
//    public JobArgument<Boolean> getCopyMailToFile() {
//        return copyMailToFile;
//    }
    
    public JobArgument<Boolean> getSaveBodyAsAttachment() {
        return saveBodyAsAttachment;
    }
//    
//    public JobArgument<Boolean> getDeleteMail() {
//        return deleteMail;
//    }
    
    public JobArgument<Boolean> getMailOnlyUnseen() {
        return mailOnlyUnseen;
    }
    
//    public JobArgument<String> getMailAction() {
//        return mailAction;
//    }
    
    public JobArgument<String> getMailBodyPattern() {
        return mailBodyPattern;
    }
    
    public JobArgument<String> getMailSubjectFilter() {
        return mailSubjectFilter;
    }
    
    public JobArgument<String> getMailSubjectPattern() {
        return mailSubjectPattern;
    }
    
    public JobArgument<String> getMailFromFilter() {
        return mailFromFilter;
    }
    
    public JobArgument<String> getAttachmentFileNamePattern() {
        return attachmentFileNamePattern;
    }
    
    public JobArgument<String> getMailDirectoryName() {
        return mailDirectoryName;
    }
    
    public JobArgument<List<String>> getMailMessageFolder() {
        return mailMessageFolder;
    }
    
//    public JobArgument<Boolean> getCopyAttachmentsToFile() {
//        return copyAttachmentsToFile;
//    }
    
    public JobArgument<String> getMinMailAge() {
        return minMailAge;
    }
    

}
