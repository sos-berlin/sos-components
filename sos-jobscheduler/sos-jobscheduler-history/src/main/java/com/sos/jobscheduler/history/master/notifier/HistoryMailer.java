package com.sos.jobscheduler.history.master.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.handler.configuration.HandlerConfiguration;
import com.sos.jobscheduler.event.master.handler.notifier.INotifier;
import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryMailer implements INotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMailer.class);
    private final HandlerConfiguration configuration;

    private static enum Range {
        RECOVERY, ERROR, WARN
    };

    public HistoryMailer(HandlerConfiguration conf) {
        configuration = conf;
    }

    public void notifyOnRecovery(String subjectPart, String bodyPart) {
        send(Range.RECOVERY, subjectPart, bodyPart, null);
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
        sb.append(HistoryUtil.NEW_LINE).append(HistoryUtil.NEW_LINE);
        sb.append("[").append(range.name()).append("]");
        if (bodyPart == null && t == null) {
            sb.append(subjectPart);
        } else {
            if (bodyPart != null) {
                sb.append(bodyPart);
            }
            if (t != null) {
                sb.append(HistoryUtil.NEW_LINE).append(HistoryUtil.NEW_LINE);
                sb.append(Throwables.getStackTraceAsString(t));
            }
        }
        return sb.toString();
    }

    private void send(Range range, String subjectPart, String bodyPart, Throwable t) {
        SOSMail mail;
        try {
            if (SOSString.isEmpty(configuration.getMailSmtpHost())) {
                return;
            }
            mail = new SOSMail(configuration.getMailSmtpHost());
            mail.setPort(configuration.getMailSmtpPort());
            mail.setUser(configuration.getMailSmtpUser());
            mail.setPassword(configuration.getMailSmtpPassword());
            mail.setFrom(configuration.getMailFrom());
            mail.addRecipient(configuration.getMailTo());

            mail.setSubject(getSubject(range, subjectPart, t));
            mail.setBody(getBody(range, subjectPart, bodyPart, t));
            mail.send();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
