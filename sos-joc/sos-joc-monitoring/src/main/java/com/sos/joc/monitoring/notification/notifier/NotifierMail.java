package com.sos.joc.monitoring.notification.notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.inventory.model.job.notification.JobNotificationMail;
import com.sos.inventory.model.job.notification.JobNotificationType;
import com.sos.joc.Globals;
import com.sos.joc.classes.history.HistoryNotification;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;
import com.sos.monitoring.notification.NotificationType;

public class NotifierMail extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierMail.class);
    private static final boolean QUEUE_MAIL_ON_ERROR = false;

    private final MonitorMail monitor;

    private SOSMail mail = null;

    public NotifierMail(int nr, MonitorMail monitor, Configuration conf) throws Exception {
        super.setNr(nr);
        this.monitor = monitor;
        init(conf);
    }

    @Override
    public AMonitor getMonitor() {
        return monitor;
    }

    @Override
    public NotifyResult notify(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos,
            DBItemNotification mn) {
        NotifyResult result = new NotifyResult(monitor.getMessage(), getSendInfo());
        if (mail == null) {
            result.setError("mail is null");
            return result;
        }

        NotifyResult skip = checkJobNotification(type, mos);
        if (skip != null) {
            return skip;
        }

        set(type, timeZone, mo, mos, mn);

        mail.setSubject(resolve(monitor.getSubject(), true));
        mail.setBody(resolve(monitor.getMessage(), true));

        try {
            StringBuilder info = new StringBuilder();
            info.append("[subject=").append(mail.getSubject()).append("]");

            LOGGER.info(getInfo4execute(true, mo, mos, type, info.toString()));

            if (!mail.send()) {
                if (QUEUE_MAIL_ON_ERROR) {
                    // - mail will be stored to the mail queue directory
                    // - a warning message will be logged by SOSMail
                } else {
                    LOGGER.error(getInfo4executeFailed(mo, mos, type, monitor.getInfo().toString()));
                }
            }
            return result;
        } catch (Throwable e) {
            result.setError(getInfo4executeFailed(mo, mos, type, "[" + monitor.getInfo().toString() + "]" + e.toString()), e);
            return result;
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

    @Override
    public StringBuilder getSendInfo() {
        return new StringBuilder("[").append(monitor.getInfo()).append("]");
    }

    private void init(Configuration conf) throws Exception {
        try {
            MailResource mr = getMailResource(conf);
            createMail(mr);
            if (SOSString.isEmpty(mail.getHost())) {
                throw new Exception(String.format("[%s][missing host][known properties]%s", monitor.getInfo(), mr.getMaskedMailProperties()));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][known properties]%s", monitor.getInfo(), mr.getMaskedMailProperties()));
            }
        } catch (Throwable e) {
            mail = null;
            throw e;
        }
    }

    private MailResource getMailResource(Configuration conf) throws Exception {
        MailResource resource = null;
        List<String> notDeployed = new ArrayList<>();
        if (monitor.getJobResources() != null) {
            if (monitor.getJobResources().size() == 1) {
                resource = conf.getMailResources().get(monitor.getJobResources().get(0));
            } else {
                List<MailResource> list = new ArrayList<>();
                for (String res : monitor.getJobResources()) {
                    MailResource r = conf.getMailResources().get(res);
                    if (r == null) {
                        notDeployed.add(res);
                    } else {
                        list.add(r);
                    }
                }

                if (notDeployed.size() > 0) {
                    throw new Exception(String.format("[monitor %s][Job Resource not deployed]%s", monitor.getInfo(), String.join(",", notDeployed)));
                }

                if (list.size() > 0) {
                    resource = new MailResource();
                    resource.parse(list);
                }
            }
        }
        if (resource == null) {
            throw new Exception(String.format("[monitor %s]missing Job Resource", monitor.getInfo()));
        }
        return resource;
    }

    private void createMail(MailResource res) throws Exception {
        mail = new SOSMail(res.copyMailProperties());
        mail.init();
        mail.setQueueMailOnError(QUEUE_MAIL_ON_ERROR);
        mail.setCredentialStoreArguments(res.getCredentialStoreArgs());
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

    private void setMailRecipients(MailResource res) throws Exception {
        mail.clearRecipients();

        // Element TO is not empty - set TO,CC,BCC from Element
        if (!SOSString.isEmpty(monitor.getTo())) {
            mail.addRecipient(monitor.getTo());
            if (!SOSString.isEmpty(monitor.getCC())) {
                mail.addCC(monitor.getCC());
            }
            if (!SOSString.isEmpty(monitor.getBCC())) {
                mail.addBCC(monitor.getBCC());
            }
        }// Element TO is empty - set TO from JobResource and CC,BCC from Element
        else if (!SOSString.isEmpty(monitor.getCC())) {
            if (!SOSString.isEmpty(res.getTo())) {
                mail.addRecipient(res.getTo());
            }
            mail.addCC(monitor.getCC());
            if (!SOSString.isEmpty(monitor.getBCC())) {
                mail.addBCC(monitor.getBCC());
            }
        }
        // Elements TO,CC are empty - set TO,CC from JobResource and BCC from Element
        else if (!SOSString.isEmpty(monitor.getBCC())) {
            if (!SOSString.isEmpty(res.getTo())) {
                mail.addRecipient(res.getTo());
            }
            if (!SOSString.isEmpty(res.getCC())) {
                mail.addRecipient(res.getCC());
            }
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

    private NotifyResult checkJobNotification(NotificationType type, DBItemMonitoringOrderStep mos) {
        if (mos != null && !SOSString.isEmpty(mos.getJobNotification())) {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            JobNotification jn = null;
            try {
                jn = Globals.objectMapper.readValue(mos.getJobNotification(), JobNotification.class);
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][job=%s][error on read job notification][%s]%s", ANotifier.getTypeAsString(type), mos.getJobName(),
                        mos.getJobNotification(), e.toString()), e);
            }
            if (HistoryNotification.isJobMailNotificationEmpty(jn)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][skip][job=%s][job notification][%s]missing settings", ANotifier.getTypeAsString(type), mos
                            .getJobName(), mos.getJobNotification()));
                }
                return null;
            } else if (jn.getMail() == null) {
                jn.setMail(new JobNotificationMail());
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][job=%s][use job notification]%s", ANotifier.getTypeAsString(type), mos.getJobName(), mos
                        .getJobNotification()));
            }

            // check job notification
            NotifyResult r = checkJobNotificationTypes(type, jn, mos.getJobName());
            if (r == null) {
                r = checkJobMailTo(type, jn, mos.getJobName());
            }
            if (r != null) {
                return r;
            }

            try {
                // suppress merge of the configured mail recipients (use job notification and ignore xml configuration)
                mail.clearRecipients();

                // add To - required
                mail.addRecipient(jn.getMail().getTo());

                // add CC - optional
                String cc = getValue(jn.getMail().getCc());
                if (cc.length() > 0) {
                    mail.addCC(cc);
                }

                // add BCC- optional
                String bcc = getValue(jn.getMail().getBcc());
                if (bcc.length() > 0) {
                    mail.addBCC(bcc);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][job=%s][error on set mail recipients][%s]%s", ANotifier.getTypeAsString(type), mos.getJobName(), mos
                        .getJobNotification(), e.toString()), e);
            }

        }
        return null;
    }

    /** check job notification types - compare with the current notification type(configured in the xml configuration) */
    private NotifyResult checkJobNotificationTypes(NotificationType type, JobNotification jn, String jobName) {
        // check if empty
        if (HistoryNotification.isJobNotificationTypesEmpty(jn.getTypes())) {
            return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, getNotConfiguredMsg(type, null), jobName, jn.getMail().getTo(),
                    jn.getMail().getCc(), jn.getMail().getBcc(), null));
        }

        // check if the job notification "mail on" types not configured in the global notification
        Set<String> types = getTypes(jn.getTypes());
        switch (type) {
        case ERROR:
        case SUCCESS:
        case WARNING:
            if (!types.contains(type.name().toUpperCase())) {
                return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, getNotConfiguredMsg(type, types), jobName, jn.getMail()
                        .getTo(), jn.getMail().getCc(), jn.getMail().getBcc(), null));
            }
            break;
        case RECOVERED:
            if (!types.contains(NotificationType.ERROR.name().toUpperCase())) {
                return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, getNotConfiguredMsg(NotificationType.ERROR, types), jobName,
                        jn.getMail().getTo(), jn.getMail().getCc(), jn.getMail().getBcc(), null));
            }
            break;
        case ACKNOWLEDGED:
            return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, "not supported", jobName, jn.getMail().getTo(), jn.getMail()
                    .getCc(), jn.getMail().getBcc(), null));
        }

        return null;
    }

    /** check required to */
    private NotifyResult checkJobMailTo(NotificationType type, JobNotification jn, String jobName) {
        if (jn.getMail() == null) {
            return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, "missing to", jobName, null, null, null, jn.getTypes()));
        }

        String to = getValue(jn.getMail().getTo());
        if (to.length() == 0) {
            return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, "missing to", jobName, to, jn.getMail().getCc(), jn.getMail()
                    .getBcc(), jn.getTypes()));
        }
        return null;
    }

    private StringBuilder getSkipCause(NotificationType type, String msg, String jobName, String to, String cc, String bcc,
            List<JobNotificationType> types) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(ANotifier.getTypeAsString(type)).append("]");
        sb.append("[").append(msg).append("]");
        sb.append("[job=").append(jobName).append("]");
        if (types != null) {
            sb.append("[mail on=").append(String.join(",", getTypes(types))).append("]");
        }
        sb.append("[");
        sb.append("to=").append(getValue(to));
        sb.append(",cc=").append(getValue(cc));
        sb.append(",bcc=").append(getValue(bcc));
        sb.append("]");
        return sb;
    }

    private Set<String> getTypes(List<JobNotificationType> types) {
        if (types == null) {
            return null;
        }
        return types.stream().map(e -> {
            return e.name().toUpperCase();
        }).collect(Collectors.toSet());
    }

    private String getNotConfiguredMsg(NotificationType type, Set<String> types) {
        if (types == null) {
            return String.format("job notification 'mail on' %s is not configured('mail on' is empty)", type.name());
        }
        return String.format("job notification 'mail on' %s is not configured('mail on'=%s)", type.name(), String.join(",", types));
    }

    private String getValue(String val) {
        if (val == null) {
            return "";
        }
        return val.trim();
    }
}
