package com.sos.joc.monitoring.notification.notifier;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.exception.SOSNotifierSendException;

public class NotifierMail extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierMail.class);
    private static final boolean QUEUE_MAIL_ON_ERROR = false;

    private final MonitorMail monitor;

    private SOSMail mail = null;

    public NotifierMail(MonitorMail monitor) {
        this.monitor = monitor;
    }

    public void init(MailResource resource) {
        try {
            if (resource == null) {
                throw new Exception("missing job_resource=" + monitor.getJobResource());
            }
            createMail(resource);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            mail = null;
        }
    }

    @Override
    public void notify(DBLayerMonitoring dbLayer, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, ServiceStatus status,
            ServiceMessagePrefix prefix) throws SOSNotifierSendException {
        if (mail == null) {
            LOGGER.warn(String.format("[%s name=\"%s\" job_resource=\"%s\"][skip]due to init error", monitor.getRefElementName(), monitor
                    .getMonitorName(), monitor.getJobResource()));
            return;
        }
        evaluate(mo, mos, status, prefix);

        mail.setSubject(resolve(monitor.getSubject(), true));
        mail.setBody(resolve(monitor.getMessage(), true));

        try {
            LOGGER.info(String.format("[%s-%s][mail]execute", getServiceStatus(), getServiceMessagePrefix()));
            if (!mail.send()) {
                if (QUEUE_MAIL_ON_ERROR) {
                    // - mail will be stored to the mail queue directory
                    // - a warning message will be logged by SOSMail
                } else {
                    throw new Exception("failed");
                }
            }
        } catch (Throwable e) {
            throw new SOSNotifierSendException(String.format("[%s name=\"%s\" job_resource=\"%s\"]can't send mail", monitor.getRefElementName(),
                    monitor.getMonitorName(), monitor.getJobResource()), e);
        }
    }

    @Override
    public void close() {
    }

    private void createMail(MailResource res) throws Exception {
        mail = new SOSMail(res.getProperties());
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
