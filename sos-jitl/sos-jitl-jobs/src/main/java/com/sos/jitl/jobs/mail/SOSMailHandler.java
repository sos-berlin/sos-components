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

public class SOSMailHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailHandler.class);
    private Map<String, Object> variables = new HashMap<>();
    private JobLogger logger = null;
    private SOSMailJobArguments args;

    public SOSMailHandler(SOSMailJobArguments args, Map<String, Object> variables, JobLogger logger) {
        this.logger = logger;
        this.variables = variables;
        this.args = args;

    }

    public void sendMail() throws Exception {
        SOSMail sosMail = null;
        try {
            Properties smtpProperties = new Properties();

            if (variables != null) {
                for (Entry<String, Object> entry : variables.entrySet()) {
                    if (entry.getValue() != null && entry.getKey().startsWith("mail.")) {
                        smtpProperties.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }

            putSmtpProperties(smtpProperties, "mail.smtp.host", args.getMailSmtpHost());
            putSmtpProperties(smtpProperties, "mail.smtp.port", String.valueOf(args.getMailSmtpPort()));
            putSmtpProperties(smtpProperties, "mail.smtp.user", args.getMailSmtpUser());
            putSmtpProperties(smtpProperties, "mail.smtp.password", args.getMailSmtpPassword());
            sosMail = new SOSMail(smtpProperties);

            sosMail.setProperties(smtpProperties);
            sosMail.setFrom(args.getFrom());

            if (args.getContentType() != null) {
                sosMail.setContentType(args.getContentType());
            }

            if (args.getEncoding() != null) {
                sosMail.setEncoding(args.getEncoding());
            }
            if (args.getCharset() != null) {
                sosMail.setCharset(args.getCharset());
            }
            if (args.getAttachmentCharset() != null) {
                sosMail.setAttachmentCharset(args.getAttachmentCharset());
            }
            if (args.getAttachmentEncoding() != null) {
                sosMail.setAttachmentEncoding(args.getAttachmentEncoding());
            }
            if (args.getAttachmentContentType() != null) {
                sosMail.setAttachmentContentType(args.getAttachmentContentType());
            }
            if (args.getFromName() != null) {
                sosMail.setFromName(args.getFromName());
            }

            sosMail.setSecurityProtocol(args.getSecurityProtocol());
            String[] recipientsTo = args.getTo().split(";|,");
            for (int i = 0; i < recipientsTo.length; i++) {
                if (i == 0) {
                    sosMail.setReplyTo(recipientsTo[i].trim());
                }
                sosMail.addRecipient(recipientsTo[i].trim());
            }
            if (args.getReplyTo() != null) {
                sosMail.setReplyTo(args.getReplyTo());
            }
            if (args.getCc() != null) {
                sosMail.addCC(args.getCc());
            }
            if (args.getBcc() != null) {
                sosMail.addBCC(args.getBcc());
            }
            if (args.getSubject() != null) {
                sosMail.setSubject(args.getSubject());
            }
            if (args.getBody() != null) {
                sosMail.setBody(args.getBody());
            }
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

    private void putSmtpProperties(Properties smtpProperties, String key, String value) {
        if (value != null) {
            smtpProperties.put(key, value);
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
