package com.sos.joc.monitoring.notification.notifier;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification.NotificationType;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;

public class NotifierMail extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierMail.class);
    private static final boolean QUEUE_MAIL_ON_ERROR = false;

    private final MonitorMail monitor;

    private SOSMail mail = null;

    public NotifierMail(MonitorMail monitor, Configuration conf) throws Exception {
        this.monitor = monitor;
        init(conf.getMailResources().get(monitor.getJobResource()));
    }

    private void init(MailResource resource) throws Exception {
        try {
            if (resource == null) {
                throw new Exception("missing job_resource=" + monitor.getJobResource());
            }
            createMail(resource);
            if (SOSString.isEmpty(mail.getHost())) {
                throw new Exception(String.format("[%s]missing host", monitor.getInfo()));
            }
        } catch (Throwable e) {
            mail = null;
            throw e;
        }
    }

    @Override
    public void notify(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType notificationType) {
        if (mail == null) {
            return;
        }
        evaluate(mo, mos, notificationType);

        mail.setSubject(resolve(monitor.getSubject(), true));
        mail.setBody(resolve(monitor.getMessage(), true));

        try {
            StringBuilder info = new StringBuilder();
            info.append("[subject=").append(mail.getSubject()).append("]");
            LOGGER.info(getInfo4execute(mo, mos, info.toString()));

            if (!mail.send()) {
                if (QUEUE_MAIL_ON_ERROR) {
                    // - mail will be stored to the mail queue directory
                    // - a warning message will be logged by SOSMail
                } else {
                    LOGGER.error(getInfo4executeException(mo, mos, monitor.getInfo().toString(), null));
                }
            }
        } catch (Throwable e) {
            LOGGER.error(getInfo4executeException(mo, mos, monitor.getInfo().toString(), e));
            LOGGER.info(SOSString.toString(mail));
        } finally {
            try {
                mail.clearRecipients();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void close() {
    }

    private void createMail(MailResource res) throws Exception {
        mail = new SOSMail(res.getProperties());
        mail.init();
        mail.setQueueMailOnError(QUEUE_MAIL_ON_ERROR);
        setMailHeaders(res);
    }

    private void setMailHeaders(MailResource res) throws Exception {
        if (!SOSString.isEmpty(monitor.getContentType())) {
            mail.setContentType(monitor.getContentType());
        }
        if (!SOSString.isEmpty(monitor.getCharset())) {
            mail.setCharset(monitor.getCharset());
        }
        if (!SOSString.isEmpty(monitor.getEncoding())) {
            mail.setEncoding(monitor.getEncoding());
        }

        addFrom(res);
        addReplayTo(res);

        setMailRecipients(res);
        setMailPriority();

    }

    private void setMailRecipients(MailResource res) throws Exception {
        mail.clearRecipients();

        addTo(res);
        addCC(res);
        addBCC(res);
    }

    private void addFrom(MailResource res) throws Exception {
        if (SOSString.isEmpty(monitor.getFrom())) {
            mail.setFrom(res.getFrom());
            if (!SOSString.isEmpty(res.getFromName())) {
                mail.setFromName(res.getFromName());
            }
        } else {
            mail.setFrom(monitor.getFrom());
        }
    }

    private void addReplayTo(MailResource res) throws Exception {
        // TODO ?
        // if (!SOSString.isEmpty(res.getReplayTo())) {
        // mail.setReplyTo(res.getReplayTo());
        // }
    }

    private void addTo(MailResource res) throws Exception {
        if (SOSString.isEmpty(monitor.getTo())) {
            mail.addRecipient(res.getTo());
        } else {
            mail.addRecipient(monitor.getTo());
        }
    }

    private void addCC(MailResource res) throws Exception {
        if (SOSString.isEmpty(monitor.getCC())) {
            if (!SOSString.isEmpty(res.getCC())) {
                mail.addCC(res.getCC());
            }
        } else {
            mail.addCC(monitor.getCC());
        }
    }

    private void addBCC(MailResource res) throws Exception {
        if (SOSString.isEmpty(monitor.getBCC())) {
            if (!SOSString.isEmpty(res.getBCC())) {
                mail.addBCC(res.getBCC());
            }
        } else {
            mail.addBCC(monitor.getBCC());
        }
    }

    private void setMailPriority() throws MessagingException {
        if (SOSString.isEmpty(monitor.getPriority())) {
            return;
        }
        switch (monitor.getPriority().toUpperCase()) {
        case "HIGHEST":
            mail.setPriorityHighest();
            break;
        case "HIGH":
            mail.setPriorityHigh();
            break;
        case "LOW":
            mail.setPriorityLow();
            break;
        case "LOWEST":
            mail.setPriorityLowest();
            break;
        }
    }
}
