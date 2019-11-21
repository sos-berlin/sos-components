package com.sos.jobscheduler.event.master.handler.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.configuration.handler.MailerConfiguration;

public class Mailer extends DefaultNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);
    private final MailerConfiguration config;

    private static enum Range {
        RECOVERY, ERROR, WARN
    };

    public Mailer(MailerConfiguration configuration) {
        config = configuration;
    }

    public void notifyOnRecovery(String subjectPart, String bodyPart) {
        send(Range.RECOVERY, subjectPart, bodyPart, null);
    }

    public void notifyOnRecovery(String subjectPart, Throwable ex) {
        send(Range.RECOVERY, subjectPart, null, ex);
    }

    public void notifyOnWarning(String bodyPart, Throwable t) {
        notifyOnWarning(null, bodyPart, t);
    }

    public void notifyOnWarning(String subjectPart, String bodyPart, Throwable t) {
        send(Range.WARN, subjectPart, bodyPart, t);
    }

    public void notifyOnError(String bodyPart, Throwable t) {
        notifyOnError(null, bodyPart, t);
    }

    public void notifyOnError(String subjectPart, String bodyPart, Throwable t) {
        send(Range.ERROR, subjectPart, bodyPart, t);
    }

    private String getExceptionName(Throwable cause) {
        Throwable e = cause;
        while (e != null) {
            String name = e.getClass().getSimpleName();
            if (!name.equalsIgnoreCase("exception")) {
                return name;
            }
            e = e.getCause();
        }
        return "Exception";
    }

    private String getSubject(Range range, String subjectPart, Throwable t) {
        StringBuilder sb = new StringBuilder("[").append(range.name().toLowerCase()).append("]");
        if (subjectPart == null) {
            if (t != null) {
                sb.append("[").append(getExceptionName(t)).append("]");
            }
        } else {
            sb.append("[").append(subjectPart).append("]");
        }
        if (range.equals(Range.RECOVERY)) {
            sb.append(" History processing recovered from previous error");
        } else if (range.equals(Range.ERROR)) {
            sb.append(" History processed with errors");
        } else if (range.equals(Range.WARN)) {
            sb.append(" History processed with warnings");
        }
        return sb.toString();
    }

    private String getBody(Range range, String subjectPart, String bodyPart, Throwable t) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(SOSDate.getCurrentTimeAsString());
        sb.append("Z ");
        sb.append(DefaultNotifier.NEW_LINE).append(DefaultNotifier.NEW_LINE);
        sb.append("[").append(range.name()).append("]");
        if (bodyPart == null && t == null) {
            sb.append(subjectPart);
        } else {
            if (bodyPart != null) {
                sb.append(bodyPart);
            }
            if (t != null) {
                sb.append(DefaultNotifier.NEW_LINE).append(DefaultNotifier.NEW_LINE);
                sb.append(Throwables.getStackTraceAsString(t));
            }
        }
        return sb.toString();
    }

    private void send(Range range, String subjectPart, String bodyPart, Throwable t) {
        SOSMail mail;
        try {
            if (SOSString.isEmpty(config.getSmtpHost())) {
                return;
            }
            mail = new SOSMail(config.getSmtpHost());
            mail.setPort(config.getSmtpPort());
            mail.setUser(config.getSmtpUser());
            mail.setPassword(config.getSmtpPassword());
            mail.setFrom(config.getFrom());
            mail.addRecipient(config.getTo());

            mail.setSubject(getSubject(range, subjectPart, t));
            mail.setBody(getBody(range, subjectPart, bodyPart, t));
            mail.send();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
