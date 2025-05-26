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

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.commons.mail.SOSMimeMessage;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

public class MailProcessor {

    private final MailInboxArguments args;

    private final ISOSLogger logger;
    private Folder inFolder;
    private Folder targetFolder;
    private Date dateMinAge = null;

    public MailProcessor(MailInboxArguments args, ISOSLogger logger) {
        this.args = args;
        this.logger = logger;
        setMinAgeDate();
    }

    private void setMinAgeDate() {
        if (args.getMinMailAge().isDirty()) {
            long minAge = SOSDate.getTimeAsSeconds(args.getMinMailAge().getValue());
            if (minAge > 0L) {
                Instant now = Instant.now();
                dateMinAge = Date.from(now.minusSeconds(minAge));
                if (logger.isDebugEnabled()) {
                    logger.debug("Min. Age defined: %1$s", dateMinAge.toString());
                }
            }
        }
    }

    private boolean isPerformMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        Date messageDate = sosMimeMessage.getSentDate();
        boolean result = true;
        if (dateMinAge != null && messageDate != null && dateMinAge.before(messageDate)) {
            if (logger.isDebugEnabled()) {
                logger.debug("message skipped due to date constraint: %s | %s", sosMimeMessage.getSubject(), messageDate.toString());
            }
            result = false;
        } else {
            logger.info("processing: %s | %s", sosMimeMessage.getSubject(), (messageDate != null) ? messageDate.toString() : "unknown date");
        }
        return result;
    }

    private void executeMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        if (isPerformMessage(sosMimeMessage)) {
            performAction(sosMimeMessage);
        }
    }

    private void performAction(final SOSMimeMessage message) throws IOException, MessagingException, SOSInvalidDataException,
            JobRequiredArgumentMissingException {
        if (args.getAction().getValue().contains(ActionProcess.dump)) {
            if (args.getMailDirectoryName().isEmpty()) {
                throw new JobRequiredArgumentMissingException("No target directory [parameter " + args.getMailDirectoryName().getName()
                        + "] specified.");
            }
            dumpMessage(message, args.getMailDirectoryName().getValue());
        }
        if (args.getAction().getValue().contains(ActionProcess.dump_attachments)) {
            if (args.getAttachmentDirectoryName().isEmpty()) {
                throw new JobRequiredArgumentMissingException("No target directory [parameter " + args.getAttachmentDirectoryName().getName()
                        + "] specified.");
            }
            copyAttachmentsToFile(message);
        }
        // if (args.getCopyMailToFile().getValue()) {
        // if (args.getMailDirectoryName().isEmpty()) {
        // throw new SOSRequiredArgumentMissingException("No target directory [parameter " + args.getMailDirectoryName().getName()
        // + "] specified.");
        // }
        // dumpMessage(message, args.getMailDirectoryName().getValue());
        // }
        //
        // if (args.getCopyAttachmentsToFile().getValue()) {
        // copyAttachmentsToFile(message);
        // }

        // if (args.getDeleteMail().getValue()) {
        // deleteMessage(message);
        // } else {
        handleAfterProcessEmail(message);
        // }
    }

    private void copyMailToFolder(SOSMimeMessage message) throws MessagingException, IOException, JobRequiredArgumentMissingException {
        if (targetFolder != null) {
            List<Message> tempList = new ArrayList<>();
            tempList.add(message.getMessage());
            Message[] m = tempList.toArray(new Message[tempList.size()]);
            inFolder.copyMessages(m, targetFolder);
        } else {
            throw new JobRequiredArgumentMissingException("Parameter '" + args.getAfterProcessMailDirectoryName().getName()
                    + "' is required but missing.");
        }

    }

    private void handleAfterProcessEmail(SOSMimeMessage message) throws MessagingException, IOException, JobRequiredArgumentMissingException {
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
                throw new JobRequiredArgumentMissingException("No target folder [parameter " + args.getAfterProcessMailDirectoryName().getName()
                        + "] specified.");
            }
            copyMailToFolder(message);
            deleteMessage(message);
            break;
        case copy:
            if (args.getAfterProcessMailDirectoryName().getValue().isEmpty()) {
                throw new JobRequiredArgumentMissingException("No target folder [parameter " + args.getAfterProcessMailDirectoryName().getName()
                        + "] specified.");
            }
            copyMailToFolder(message);
            break;
        }
    }

    private String getEmailFolderName(JobArgument<String> folder) throws JobRequiredArgumentMissingException {
        switch (args.getAfterProcessMail().getValue()) {
        case move:
        case copy:
            if (folder.isEmpty()) {
                throw new JobRequiredArgumentMissingException("Parameter '" + folder.getName() + "' is required but missing.");
            } else {
                return folder.getValue();
            }
        default:
            break;
        }
        return "";
    }

    public void performMessagesInFolder(SOSMailReceiver mailReader, final String messageFolder) throws MessagingException,
            JobRequiredArgumentMissingException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("reading %s", messageFolder);
            }
            inFolder = mailReader.openFolder(messageFolder, mailReader.READ_WRITE);

            String targetFolderName = getEmailFolderName(args.getAfterProcessMailDirectoryName());
            if (!targetFolderName.isEmpty()) {
                targetFolder = mailReader.openFolder(targetFolderName, mailReader.READ_WRITE);
            }
            int objectsInFolder = inFolder.getMessageCount();
            Message[] msgs = null;
            Message[] msgs2 = null;
            int maxMailsToProcess = args.getMaxMailsToProcess().getValue();

            if (objectsInFolder > maxMailsToProcess && maxMailsToProcess > 0) {
                msgs = inFolder.getMessages(objectsInFolder - maxMailsToProcess + 1, objectsInFolder);
            } else {
                msgs = inFolder.getMessages();
            }
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
                if (logger.isDebugEnabled()) {
                    logger.debug("looking for subject=%s and from=%s", args.getMailSubjectFilter().getValue(), args.getMailFromFilter().getValue());
                }
                msgs2 = inFolder.search(searchTerm, msgs);
                if (logger.isDebugEnabled()) {
                    logger.debug("%d messages found with subject=%s and from=%s", msgs2.length, args.getMailSubjectFilter().getValue(), args
                            .getMailFromFilter().getValue());
                }
            } else {

                if (!args.getMailSubjectFilter().isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("looking for subject=%s", args.getMailSubjectFilter().getValue());
                    }
                    SubjectTerm subjectTerm = new SubjectTerm(args.getMailSubjectFilter().getValue());
                    msgs2 = inFolder.search(subjectTerm, msgs);
                    if (logger.isDebugEnabled()) {
                        logger.debug("%d messages found with subject=%s", msgs2.length, args.getMailSubjectFilter().getValue());
                    }
                } else if (!args.getMailFromFilter().isEmpty()) {
                    FromTerm fromTerm = new FromTerm(new InternetAddress(args.getMailFromFilter().getValue()));
                    if (logger.isDebugEnabled()) {
                        logger.debug("looking for from=%s", args.getMailFromFilter().getValue());
                    }
                    msgs2 = inFolder.search(fromTerm, msgs);
                    if (logger.isDebugEnabled()) {
                        logger.debug("%d messages found with from=%s", msgs2.length, args.getMailFromFilter().getValue());
                    }
                } else {
                    msgs2 = msgs;
                    logger.debug("%d messages found, folder = %s", msgs2.length, messageFolder);
                }
            }

            if (msgs2.length > 0) {
                int notUnreadMails = 0;
                for (Message messageElement : msgs2) {
                    if (args.getMailOnlyUnseen().getValue() && messageElement.isSet(Flags.Flag.SEEN)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("message skipped, already seen: %s", messageElement.getSubject());
                        }
                        notUnreadMails++;
                        continue;
                    }
                    try {
                        // instantiate the mail item without additional information, so that not each mailItem has all information
                        // e.g. when checking the mail item with the patterns, we do not want to have all attachments loaded as well
                        SOSMimeMessage sosMailItem = new SOSMimeMessage(messageElement, true);
                        if (subjectPattern != null) {
                            if (!subjectPattern.matcher(sosMailItem.getSubject()).find()) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("message skipped, subject does not match [%1$s]: %2$s", args.getMailSubjectPattern().getValue(),
                                            sosMailItem.getSubject());
                                }
                                continue;
                            }
                        }
                        if (bodyPattern != null) {
                            if (!bodyPattern.matcher(sosMailItem.getPlainTextBody()).find()) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("message with subject %s skipped, body does not match [%s]", sosMailItem.getSubject(), args
                                            .getMailBodyPattern().getValue());
                                }
                                continue;
                            }
                        }
                        // mailItem is used further, now is the time to initiate the additional information
                        // e.g. if the mail has attachments we can load them now for further processing
                        sosMailItem.init();
                        executeMessage(sosMailItem);
                    } catch (Exception e) {
                        logger.info("message '%s' skipped, exception occured: %s", messageElement.getSubject(), e.toString());
                        continue;
                    }
                }
                if (notUnreadMails > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("%d messages skipped because they are already read", notUnreadMails);
                    }
                }
            }
        } finally {
            mailReader.closeFolder(true);
        }
    }

    private void dumpMessage(final SOSMimeMessage message, String directory) throws IOException, MessagingException {
        File messageFile = new File(directory, message.getMessageId());
        if (logger.isDebugEnabled()) {
            logger.debug("dump message. subject=%s, date=%s, file=%s: ", message.getSubject(), message.getSentDateAsString(), messageFile);
        }
        message.dumpMessageToFile(messageFile, true, false);
    }

    private void deleteMessage(final SOSMimeMessage message) throws UnsupportedEncodingException, MessagingException {
        if (logger.isDebugEnabled()) {
            logger.debug("deleting message. subject=%s, date=%s", message.getSubject(), message.getSentDateAsString());
        }
        message.deleteMessage();
    }

    private void copyAttachmentsToFile(final SOSMimeMessage message) throws SOSInvalidDataException, IOException, MessagingException {
        String directory = args.getAttachmentDirectoryName().getValue();
        if (logger.isDebugEnabled()) {
            logger.debug("saving attachments. subject=%s, date=%s, directory=%s: ", message.getSubject(), message.getSentDateAsString(), directory);
        }
        message.saveAttachments(message, args.getAttachmentFileNamePattern().getValue(), directory, args.getSaveBodyAsAttachment().getValue())
                .forEach(s -> logger.debug("attachment file [%s] successfully saved.", s));
    }
}
