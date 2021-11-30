package com.sos.jitl.jobs.mail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.commons.mail.SOSMimeMessage;
import com.sos.commons.util.SOSDate;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;


public class MailProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailProcessor.class);
    private final MailInboxArguments args;
    private final JobLogger jobLogger;
    private Folder inFolder;
    private Folder targetFolder;
    private Date dateMinAge = null;
    
    public MailProcessor(MailInboxArguments args, JobLogger logger) {
        this.args = args;
        this.jobLogger = logger;
        setMinAgeDate();
    }

    private void setMinAgeDate() { 
        if (args.getMinMailAge().isDirty()) {
            long minAge = SOSDate.getTimeAsSeconds(args.getMinMailAge().getValue());
            if (minAge > 0L) {
                Instant now = Instant.now(); 
                dateMinAge = Date.from(now.minusSeconds(minAge));
                logDebug("Min. Age defined: %1$s", dateMinAge.toString());
            }
        }
    }

    private boolean isPerformMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        Date messageDate = sosMimeMessage.getSentDate();
        boolean result = true;
        if (dateMinAge != null && messageDate != null && dateMinAge.before(messageDate)) {
            logDebug("message skipped due to date constraint: %s | %s", sosMimeMessage.getSubject(), messageDate.toString());
            result = false;
        } else {
            logInfo("processing: %s | %s", sosMimeMessage.getSubject(), (messageDate != null) ? messageDate.toString() : "unknown date");
        }
        return result;
    }

    private void executeMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        if (isPerformMessage(sosMimeMessage)) {
            performAction(sosMimeMessage);
        }
    }

    private void performAction(final SOSMimeMessage message) throws IOException, MessagingException, SOSInvalidDataException,
            SOSJobRequiredArgumentMissingException {
        if (args.getAction().getValue().contains(ActionProcess.dump)) {
            if (args.getMailDirectoryName().isEmpty()) {
                throw new SOSJobRequiredArgumentMissingException("No target directory [parameter " + args.getMailDirectoryName().getName()
                        + "] specified.");
            }
            dumpMessage(message, args.getMailDirectoryName().getValue());
        }
        if (args.getAction().getValue().contains(ActionProcess.dump_attachments)) {
            if (args.getAttachmentDirectoryName().isEmpty()) {
                throw new SOSJobRequiredArgumentMissingException("No target directory [parameter " + args.getAttachmentDirectoryName().getName()
                        + "] specified.");
            }
            copyAttachmentsToFile(message);
        }
//        if (args.getCopyMailToFile().getValue()) {
//            if (args.getMailDirectoryName().isEmpty()) {
//                throw new SOSRequiredArgumentMissingException("No target directory [parameter " + args.getMailDirectoryName().getName()
//                        + "] specified.");
//            }
//            dumpMessage(message, args.getMailDirectoryName().getValue());
//        }
//
//        if (args.getCopyAttachmentsToFile().getValue()) {
//            copyAttachmentsToFile(message);
//        }

//        if (args.getDeleteMail().getValue()) {
//            deleteMessage(message);
//        } else {
            handleAfterProcessEmail(message);
//        }
    }

    private void copyMailToFolder(SOSMimeMessage message) throws MessagingException, IOException, SOSJobRequiredArgumentMissingException {
        if (targetFolder != null) {
            List<Message> tempList = new ArrayList<>();
            tempList.add(message.getMessage());
            Message[] m = tempList.toArray(new Message[tempList.size()]);
            inFolder.copyMessages(m, targetFolder);
        } else {
            throw new SOSJobRequiredArgumentMissingException("Parameter '" + args.getAfterProcessMailDirectoryName().getName()
                    + "' is required but missing.");
        }

    }

    private void handleAfterProcessEmail(SOSMimeMessage message) throws MessagingException, IOException, SOSJobRequiredArgumentMissingException {
        switch (args.getAfterProcessMail().getValue()) {
        case none:
            break;
        case mark_as_read:
        case markAsRead:
            message.setFlag(Flags.Flag.SEEN, true);
            break;
        case delete:
            deleteMessage(message);
            break;
        case move:
            if (args.getAfterProcessMailDirectoryName().getValue().isEmpty()) {
                throw new SOSJobRequiredArgumentMissingException("No target folder [parameter " + args.getAfterProcessMailDirectoryName().getName()
                        + "] specified.");
            }
            copyMailToFolder(message);
            deleteMessage(message);
            break;
        case copy:
            if (args.getAfterProcessMailDirectoryName().getValue().isEmpty()) {
                throw new SOSJobRequiredArgumentMissingException("No target folder [parameter " + args.getAfterProcessMailDirectoryName().getName()
                        + "] specified.");
            }
            copyMailToFolder(message);
            break;
        }
    }

    private String getEmailFolderName(JobArgument<String> folder) throws SOSJobRequiredArgumentMissingException {
        switch (args.getAfterProcessMail().getValue()) {
        case move:
        case copy:
            if (folder.isEmpty()) {
               throw new SOSJobRequiredArgumentMissingException("Parameter '" + folder.getName() + "' is required but missing.");
            } else {
               return folder.getValue();
            }
        default:
            break;
        }
        return "";
    }

    public void performMessagesInFolder(SOSMailReceiver mailReader, final String messageFolder) throws MessagingException, SOSJobRequiredArgumentMissingException {
        try {
            logDebug("reading " + messageFolder);
            inFolder = mailReader.openFolder(messageFolder, mailReader.READ_WRITE);

            String targetFolderName = getEmailFolderName(args.getAfterProcessMailDirectoryName());
            if (!targetFolderName.isEmpty()) {
                targetFolder = mailReader.openFolder(targetFolderName, mailReader.READ_WRITE);
            }
            int maxObjectsToProcess = inFolder.getMessageCount();
            Message[] msgs = null;
            Message[] msgs2 = null;
            int intBufferSize = args.getMaxMailsToProcess().getValue();

            if (maxObjectsToProcess > intBufferSize && intBufferSize > 0) {
                maxObjectsToProcess = intBufferSize;
            }
            msgs = inFolder.getMessages(1, maxObjectsToProcess);
            Pattern subjectPattern = null;
            Pattern bodyPattern = null;
            // to compile the pattern only once before the loop and not in the loop with each round
            if (!args.getMailSubjectPattern().isEmpty()) {
                subjectPattern = Pattern.compile(args.getMailSubjectPattern().getValue());
            }
            if (!args.getMailBodyPattern().isEmpty()) {
                bodyPattern = Pattern.compile(args.getMailBodyPattern().getValue());
            }

            if (!args.getMailSubjectFilter().isEmpty() && !args.getMailFromFilter().isEmpty()) {
                FromTerm fromTerm = new FromTerm(new InternetAddress(args.getMailFromFilter().getValue()));
                SubjectTerm subjectTerm = new SubjectTerm(args.getMailSubjectFilter().getValue());
                SearchTerm searchTerm = new AndTerm(subjectTerm, fromTerm);
                logDebug("looking for subject=%s and from=%s", args.getMailSubjectFilter().getValue(), args.getMailFromFilter().getValue());
                msgs2 = inFolder.search(searchTerm, msgs);
                logDebug("%d messages found with subject=%s and from=%s", msgs2.length, args.getMailSubjectFilter().getValue(), args.getMailFromFilter()
                        .getValue());
            } else {

                if (!args.getMailSubjectFilter().isEmpty()) {
                    logDebug("looking for subject=%s", args.getMailSubjectFilter().getValue());
                    SubjectTerm subjectTerm = new SubjectTerm(args.getMailSubjectFilter().getValue());
                    msgs2 = inFolder.search(subjectTerm, msgs);
                    logDebug("%d messages found with subject=%s", msgs2.length, args.getMailSubjectFilter().getValue());
                } else if (!args.getMailFromFilter().isEmpty()) {
                    FromTerm fromTerm = new FromTerm(new InternetAddress(args.getMailFromFilter().getValue()));
                    logDebug("looking for from=%s", args.getMailFromFilter().getValue());
                    msgs2 = inFolder.search(fromTerm, msgs);
                    logDebug("%d messages found with from=%s", msgs2.length, args.getMailFromFilter().getValue());
                } else {
                    msgs2 = msgs;
                    logDebug("%d messages found, folder = %s", msgs2.length,  messageFolder);
                }
            }

            if (msgs2.length > 0) {
                int notUnreadMails = 0;
                for (Message messageElement : msgs2) {
                    if (args.getMailOnlyUnseen().getValue() && messageElement.isSet(Flags.Flag.SEEN)) {
                        logTrace("message skipped, already seen: %s", messageElement.getSubject());
                        notUnreadMails++;
                        continue;
                    }
                    try {
                        // instantiate the mail item without additional information, so that not each mailItem has all information
                        // e.g. when checking the mail item with the patterns, we do not want to have all attachments loaded as well
                        SOSMimeMessage sosMailItem = new SOSMimeMessage(messageElement, true);
                        if (subjectPattern != null) {
                            if (!subjectPattern.matcher(sosMailItem.getSubject()).find()) {
                                logTrace("message skipped, subject does not match [%1$s]: %2$s", args.getMailSubjectPattern().getValue(), sosMailItem
                                        .getSubject());
                                continue;
                            }
                        }
                        if (bodyPattern != null) {
                            if (!bodyPattern.matcher(sosMailItem.getPlainTextBody()).find()) {
                                logTrace("message with subject %s skipped, body does not match [%s]", sosMailItem.getSubject(), args.getMailBodyPattern()
                                        .getValue());
                                continue;
                            }
                        }
                        // mailItem is used further, now is the time to initiate the additional information
                        // e.g. if the mail has attachments we can load them now for further processing
                        sosMailItem.init();
                        executeMessage(sosMailItem);
                    } catch (Exception e) {
                        logInfo("message '%s' skipped, exception occured: %s", messageElement.getSubject(), e.toString());
                        continue;
                    }
                }
                if (notUnreadMails > 0) {
                    logDebug("%d messages skipped because they are already read", notUnreadMails);
                }
            }
        } finally {
            mailReader.closeFolder(true);
        }
    }

    private void dumpMessage(final SOSMimeMessage message, String directory) throws IOException, MessagingException {
        File messageFile = new File(directory, message.getMessageId());
        logDebug("dump message. subject=%s, date=%s, file=%s: ", message.getSubject(), message.getSentDateAsString(), messageFile);
        message.dumpMessageToFile(messageFile, true, false);
    }

    private void deleteMessage(final SOSMimeMessage message) throws UnsupportedEncodingException, MessagingException {
        logDebug("deleting message. subject=%s, date=%s", message.getSubject(), message.getSentDateAsString());
        message.deleteMessage();
    }

    private void copyAttachmentsToFile(final SOSMimeMessage message) throws SOSInvalidDataException, IOException, MessagingException {
        String directory = args.getAttachmentDirectoryName().getValue();
        logDebug("saving attachments. subject=%s, date=%s, directory=%s: ", message.getSubject(), message.getSentDateAsString(), directory);
        message.saveAttachments(message, args.getAttachmentFileNamePattern().getValue(), directory, args.getSaveBodyAsAttachment().getValue());
    }
    
//    private void logError(String format, Object... msg) {
//        if (jobLogger != null) {
//            jobLogger.error(format, msg);
//        } else {
//            if (msg.length == 0) {
//                LOGGER.error(format);
//            } else {
//                LOGGER.error(String.format(format, msg));
//            }
//        }
//    }
//
//    private void logError(String msg, Throwable t) {
//        if (jobLogger != null) {
//            jobLogger.error(msg, t);
//        } else {
//            LOGGER.error(msg, t);
//        }
//    }

    private void logInfo(String format, Object... msg) {
        if (jobLogger != null) {
            jobLogger.info(format, msg);
        } else {
            if (msg.length == 0) {
                LOGGER.info(format);
            } else {
                LOGGER.info(String.format(format, msg));
            }
        }
    }

    private void logDebug(String format, Object... msg) {
        if (jobLogger != null) {
            jobLogger.debug(format, msg);
        } else {
            if (msg.length == 0) {
                LOGGER.debug(format);
            } else {
                LOGGER.debug(String.format(format, msg));
            }
        }
    }
    
    private void logTrace(String format, Object... msg) {
        if (jobLogger != null) {
            jobLogger.trace(format, msg);
        } else {
            if (msg.length == 0) {
                LOGGER.trace(format);
            } else {
                LOGGER.trace(String.format(format, msg));
            }
        }
    }

//    private void logWarn(String format, Object... msg) {
//        if (jobLogger != null) {
//            jobLogger.warn(format, msg);
//        } else {
//            if (msg.length == 0) {
//                LOGGER.warn(format);
//            } else {
//                LOGGER.warn(String.format(format, msg));
//            }
//        }
//    }

}
