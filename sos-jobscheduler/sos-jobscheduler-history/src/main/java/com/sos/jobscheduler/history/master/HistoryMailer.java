package com.sos.jobscheduler.history.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;
import com.sos.jobscheduler.event.master.handler.ISender;

public class HistoryMailer implements ISender {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMailer.class);
    private static final String NEW_LINE = "\r\n";
    private final EventHandlerSettings settings;

    public HistoryMailer(EventHandlerSettings st) {
        settings = st;
    }

    public void sendOnError(String bodyPart, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(bodyPart);
        sb.append(String.format("%s%s", NEW_LINE, NEW_LINE));
        sb.append(getStackTrace(t));
        send("ERROR", "[error] History processed with errors", sb.toString());
    }

    public void sendOnError(Throwable t) {
        send("ERROR", "[error] History processed with errors", getStackTrace(t));
    }

    private String getStackTrace(Throwable t) {
        return t == null ? "null" : Throwables.getStackTraceAsString(t);
    }

    private void send(String range, String subject, String body) {
        SOSMail mail;
        try {
            if (SOSString.isEmpty(settings.getMailSmtpHost())) {
                return;
            }

            mail = new SOSMail(settings.getMailSmtpHost());

            mail.setPort(settings.getMailSmtpPort());
            mail.setUser(settings.getMailSmtpUser() == null ? "" : settings.getMailSmtpUser());
            mail.setPassword(settings.getMailSmtpPassword());
            mail.setFrom(settings.getMailFrom());
            mail.addRecipient(settings.getMailTo());

            mail.setSubject(subject);

            StringBuilder sb = new StringBuilder();
            sb.append(SOSDate.getCurrentTimeAsString());
            sb.append("Z ");
            sb.append(settings.getMailFrom());
            sb.append(String.format("%s%s", NEW_LINE, NEW_LINE));
            sb.append(range);
            sb.append(body);

            mail.setBody(body);
            mail.send();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
