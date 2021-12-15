package com.sos.joc.monitoring.notification.notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.joc.Globals;
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
    public NotifyResult notify(NotificationType type, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, DBItemNotification mn) {
        if (mail == null) {
            return new NotifyResult(monitor.getMessage(), getSendInfo(), "mail is null");
        }

        NotifyResult skip = checkJobNotification(type, mos);
        if (skip != null) {
            return skip;
        }

        set(type, mo, mos, mn);

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
                    LOGGER.error(getInfo4executeException(mo, mos, type, monitor.getInfo().toString(), null));
                }
            }
            return new NotifyResult(mail.getBody(), getSendInfo());
        } catch (Throwable e) {
            return new NotifyResult(mail.getBody(), getSendInfo(), getInfo4executeException(mo, mos, type, "[" + monitor.getInfo().toString() + "]",
                    e));
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
                throw new Exception(String.format("[%s][missing host][known properties]%s", monitor.getInfo(), mr.getMaskedProperties()));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][known properties]%s", monitor.getInfo(), mr.getMaskedProperties()));
            }
        } catch (Throwable e) {
            mail = null;
            throw e;
        }
    }

    private MailResource getMailResource(Configuration conf) throws Exception {
        MailResource resource = null;
        if (monitor.getJobResources() != null) {
            if (monitor.getJobResources().size() == 1) {
                resource = conf.getMailResources().get(monitor.getJobResources().get(0));
            } else {
                List<MailResource> list = new ArrayList<>();
                for (String res : monitor.getJobResources()) {
                    MailResource r = conf.getMailResources().get(res);
                    list.add(r);
                }
                if (list.size() > 0) {
                    resource = new MailResource();
                    resource.parse(list);
                }
            }
        }
        if (resource == null) {
            throw new Exception("missing job_resources=" + monitor.getJobResources());
        }
        return resource;
    }

    private void createMail(MailResource res) throws Exception {
        mail = new SOSMail(res.copyProperties());
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
            try {
                JobNotification jn = Globals.objectMapper.readValue(mos.getJobNotification(), JobNotification.class);
                if (jn != null && jn.getMail() != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][job=%s][use job notification]%s", ANotifier.getTypeAsString(type), mos.getJobName(), mos
                                .getJobNotification()));
                    }

                    // check job notification
                    NotifyResult r = checkJobMailSuppress(type, jn, mos.getJobName());
                    if (r == null) {
                        r = checkJobMailTo(type, jn, mos.getJobName());
                        if (r == null) {
                            r = checkJobNotificationTypes(type, jn, mos.getJobName());
                        }
                    }
                    if (r != null) {
                        return r;
                    }

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

                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][job=%s][job notification][%s]missing settings", ANotifier.getTypeAsString(type), mos
                                .getJobName(), mos.getJobNotification()));
                    }
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[type][job=%s][error on read job notification][%s]%s", ANotifier.getTypeAsString(type), mos.getJobName(),
                        mos.getJobNotification(), e.toString()), e);
            }
        }
        return null;
    }

    /** check mail suppress */
    private NotifyResult checkJobMailSuppress(NotificationType type, JobNotification jn, String jobName) {
        if (jn.getMail().getSuppress() != null && jn.getMail().getSuppress()) {
            return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, "suppress=true", jobName, jn.getMail().getTo(), jn.getMail()
                    .getCc(), jn.getMail().getBcc()));
        }
        return null;
    }

    /** check required to */
    private NotifyResult checkJobMailTo(NotificationType type, JobNotification jn, String jobName) {
        String to = getValue(jn.getMail().getTo());
        if (to.length() == 0) {
            return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, "missing to", jobName, to, jn.getMail().getCc(), jn.getMail()
                    .getBcc()));
        }
        return null;
    }

    /** check job notification types - compare with the current notification type(configured in the xml configuration) */
    private NotifyResult checkJobNotificationTypes(NotificationType type, JobNotification jn, String jobName) {
        if (jn.getTypes() != null && jn.getTypes().size() > 0) {
            Set<String> types = jn.getTypes().stream().map(e -> {
                return e.name().toUpperCase();
            }).collect(Collectors.toSet());
            switch (type) {
            case ERROR:
            case SUCCESS:
            case WARNING:
                if (!types.contains(type.name().toUpperCase())) {
                    return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, getNotConfiguredMsg(type, types), jobName, jn.getMail()
                            .getTo(), jn.getMail().getCc(), jn.getMail().getBcc()));
                }
                break;
            case RECOVERED:
                if (!types.contains(NotificationType.ERROR.name().toUpperCase())) {
                    return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, getNotConfiguredMsg(NotificationType.ERROR, types),
                            jobName, jn.getMail().getTo(), jn.getMail().getCc(), jn.getMail().getBcc()));
                }
                break;
            case ACKNOWLEDGED:
                return new NotifyResult(mail.getBody(), getSendInfo(), getSkipCause(type, "not supported", jobName, jn.getMail().getTo(), jn.getMail()
                        .getCc(), jn.getMail().getBcc()));
            }
        }
        return null;
    }

    private StringBuilder getSkipCause(NotificationType type, String msg, String jobName, String to, String cc, String bcc) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(ANotifier.getTypeAsString(type)).append("]");
        sb.append("[").append(msg).append("]");
        sb.append("[job=").append(jobName).append("]");
        sb.append("[");
        sb.append("to=").append(getValue(to));
        sb.append(",cc=").append(getValue(cc));
        sb.append(",bcc=").append(getValue(bcc));
        sb.append("]");
        return sb;
    }

    private String getNotConfiguredMsg(NotificationType type, Set<String> types) {
        return String.format("% is not configured(configured=%s)", type.name(), String.join(",", types));
    }

    private String getValue(String val) {
        if (val == null) {
            return "";
        }
        return val.trim();
    }
}
