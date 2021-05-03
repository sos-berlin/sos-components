package com.sos.jitl.jobs.mail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.mail.SOSMailAttachment;
import com.sos.jitl.jobs.common.JobLogger;

import js7.data.value.Value;

public class SOSMailHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailHandler.class);
    private Map<String, Value> variables = new HashMap<String, Value>();
    private JobLogger logger = null;
    private SOSMailJobArguments args;

    public SOSMailHandler(SOSMailJobArguments args, Map<String, Value> variables, JobLogger logger) {
        this.logger = logger;
        this.variables = variables;
        this.args = args;

    }

    public void sendMail() throws Exception {
        SOSMail sosMail = null;
        try {
            Properties smtpProperties = new Properties();

            for (Entry<String, Value> entry : variables.entrySet()) {
                if (entry.getKey().startsWith("mail_smtp_")) {
                    smtpProperties.put(entry.getKey().replaceAll("_", "."), entry.getValue().convertToString());
                }
            }

            sosMail = new SOSMail(args.getHost());
            sosMail.setProperties(smtpProperties);
            // sosMail.setQueueDir(queueDir);
            sosMail.setFrom(args.getFrom());

            if (smtpProperties.get("mail.smtp.timeout") != null) {
                try {
                    sosMail.setTimeout(Integer.parseInt(smtpProperties.get("mail.smtp.timeout").toString()));
                } catch (NumberFormatException e) {
                }
            }

            if (smtpProperties.getProperty("mail.smtp.user") != null && !smtpProperties.getProperty("mail.smtp.user").isEmpty()) {
                sosMail.setUser(smtpProperties.getProperty("mail.smtp.user"));
            }
            if (smtpProperties.getProperty("mail.smtp.password") != null && !smtpProperties.getProperty("mail.smtp.password").isEmpty()) {
                sosMail.setPassword(smtpProperties.getProperty("mail.smtp.password"));
            }
            if (smtpProperties.getProperty("mail.smtp.port") != null && !smtpProperties.getProperty("mail.smtp.port").isEmpty()) {
                sosMail.setPort(smtpProperties.getProperty("mail.smtp.port"));
            }

            if (!args.getContentType().isEmpty()) {
                sosMail.setContentType(args.getContentType());
            }

            if (!args.getEncoding().isEmpty()) {
                sosMail.setEncoding(args.getEncoding());
            }
            if (!args.getCharset().isEmpty()) {
                sosMail.setCharset(args.getCharset());
            }
            if (!args.getAttachmentCharset().isEmpty()) {
                sosMail.setAttachmentCharset(args.getAttachmentCharset());
            }
            if (!args.getAttachmentEncoding().isEmpty()) {
                sosMail.setAttachmentEncoding(args.getAttachmentEncoding());
            }
            if (!args.getAttachmentContentType().isEmpty()) {
                sosMail.setAttachmentContentType(args.getAttachmentContentType());
            }
            if (!args.getFromName().isEmpty()) {
                sosMail.setFromName(args.getFromName());
            }
            // sosMail.setQueueFailedPraefix(queueFailedPrefix);
            sosMail.setSecurityProtocol(args.getSecurityProtocol());
            String[] recipientsTo = args.getTo().split(";|,");
            for (int i = 0; i < recipientsTo.length; i++) {
                if (i == 0) {
                    sosMail.setReplyTo(recipientsTo[i].trim());
                }
                sosMail.addRecipient(recipientsTo[i].trim());
            }
            if (!args.getReplyTo().isEmpty()) {
                sosMail.setReplyTo(args.getReplyTo());
            }
            sosMail.addCC(args.getCc());
            sosMail.addBCC(args.getBcc());
            sosMail.setSubject(args.getSubject());
            sosMail.setBody(args.getBody());
            if (args.getAttachments() != null) {
                for (String attachment2 : args.getAttachments()) {
                    File attachmentFile = new File(attachment2);
                    SOSMailAttachment attachment = new SOSMailAttachment(sosMail, attachmentFile);
                    attachment.setCharset(sosMail.getAttachmentCharset());
                    attachment.setEncoding(sosMail.getAttachmentEncoding());
                    attachment.setContentType(sosMail.getAttachmentContentType());
                    sosMail.addAttachment(attachment);
                }
            }
            log(logger, "sending mail: \n" + sosMail.dumpMessageAsString());
            // sosMail.setQueueMailOnError(queueMailOnError);
            if (!sosMail.send()) {
                log(logger, "mail server is unavailable, mail for recipient [" + args.getTo() + "]" + sosMail.getLastError());
            }
            if (args.getCleanupAttachment()) {
                for (String attachment : args.getAttachments()) {
                    File attachmentFile = new File(attachment);
                    if (attachmentFile.exists() && attachmentFile.canWrite()) {
                        attachmentFile.delete();
                    }
                }
            }
            sosMail.clearRecipients();
        } catch (

        Exception e) {
            e.printStackTrace();
            if (sosMail == null) {
                log(logger, e.getMessage());
            } else {
                log(logger, sosMail.getLastError() + ":" + e.getMessage());
            }
            throw new Exception(e.getMessage());
        }

    }

    private void log(JobLogger logger, String log) {
        LOGGER.info(log);
        if (logger != null) {
            logger.info(log);
        }
    }

    private void debug(JobLogger logger, String log) {
        LOGGER.debug(log);
        if (logger != null) {
            logger.debug(log);
        }
    }
}
