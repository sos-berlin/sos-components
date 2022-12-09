package com.sos.joc.monitoring.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.configuration.objects.workflow.Workflow;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob.CriticalityType;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public abstract class AConfiguration {

    public static final String LOG_INTENT = "    ";
    public static final String LOG_INTENT_2 = LOG_INTENT + LOG_INTENT;

    private static final Logger LOGGER = LoggerFactory.getLogger(AConfiguration.class);

    private static final String LOG_FIND_START = LOG_INTENT + "[find][start]";
    private static final String LOG_FIND_END = LOG_INTENT + "[find][end]";

    private static String JOC_TITLE;
    private static String JOC_URI;
    private static String JOC_REVERSE_PROXY_URI;

    private SystemNotification systemNotification;

    private List<Notification> onError;
    private List<Notification> onWarning;
    private List<Notification> onSuccess;
    private Map<String, MailResource> mailResources;

    private boolean exists;

    public void loadIfNotExists(String caller, String jocTitle, String jocUri) {
        if (exists) {
            if (LOGGER.isDebugEnabled()) {
                MonitorService.setLogger();
                LOGGER.debug(String.format("[%s][configuration]already loaded", caller));
            }
        } else {
            load(caller, jocTitle, jocUri);
        }
    }

    public void clear() {
        init();
    }

    public synchronized void load(String caller, String jocTitle, String jocUri) {

        boolean run = true;
        int errorCount = 0;
        while (run) {
            DBLayerMonitoring dbLayer = new DBLayerMonitoring(MonitorService.getIdentifier(caller));
            try {
                MonitorService.setLogger();

                dbLayer.setSession(Globals.createSosHibernateStatelessConnection(dbLayer.getIdentifier()));
                String configXml = dbLayer.getReleasedConfiguration();

                if (!SOSString.isEmpty(jocTitle)) {
                    JOC_TITLE = jocTitle;
                }
                if (!SOSString.isEmpty(jocUri)) {
                    JOC_URI = jocUri;
                }
                setJocReverseProxyUri(Globals.getConfigurationGlobalsJoc().getJocReverseProxyUrl().getValue());
                process(configXml);
                if (exists) {
                    List<String> names = handleMailResources(dbLayer);

                    LOGGER.info(String.format("[%s][configuration][SystemNotification=%s][Notifications type %s=%s,%s=%s,%s=%s][JobResources=%s]",
                            caller, getSystemNotificationInfo(), NotificationType.ERROR.name(), onError.size(), NotificationType.WARNING.name(),
                            onWarning.size(), NotificationType.SUCCESS.name(), onSuccess.size(), String.join(",", names)));
                } else {
                    LOGGER.info(String.format("[%s][configuration]exists=false", caller));
                }
                run = false;
                errorCount = 0;
            } catch (Exception e) {
                errorCount++;
                LOGGER.error(String.format("[%s][errorCount=%s]%s", caller, errorCount, e.toString()), e);
            } finally {
                dbLayer.close();
            }

            if (run) {
                if (errorCount >= 3) {
                    run = false;
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e1) {
                        LOGGER.error(String.format("[%s][interrupted]%s", caller, e1.toString()), e1);
                        run = false;
                    }
                }
            }
        }
    }

    private String getSystemNotificationInfo() {
        if (systemNotification == null) {
            return "";
        }
        return systemNotification.getTypesAsString();
    }

    private List<String> handleMailResources(DBLayerMonitoring dbLayer) throws Exception {
        if (mailResources == null || mailResources.size() == 0) {
            return new ArrayList<>();
        }

        List<String> names = mailResources.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        // name,content
        List<Object[]> resources = dbLayer.getDeployedJobResources(names);
        if (resources != null) {
            for (Object[] r : resources) {
                String name = r[0].toString();

                MailResource mr = mailResources.get(name);
                mr.parse(name, r[1].toString());
                mailResources.put(name, mr);
            }
            if (resources.size() != mailResources.size()) {// some configured resources were not found in the database
                List<String> toRemove = mailResources.entrySet().stream().filter(e -> {
                    return e.getValue().getMailProperties() == null;
                }).map(Map.Entry::getKey).collect(Collectors.toList());

                if (toRemove.size() > 0) {
                    LOGGER.warn(String.format("[Job Resource=%s]configured Job Resource not found in the deployment history", String.join(",",
                            toRemove)));

                    for (String name : toRemove) {
                        mailResources.remove(name);
                    }
                }
            }
        }
        return names;
    }

    public String getJocTitle() {
        return JOC_TITLE;
    }

    public String getJocUri() {
        if (SOSString.isEmpty(JOC_URI)) {
            JOC_URI = getJocBaseUri();
        }
        return JOC_URI;
    }

    public void setJocReverseProxyUri(String val) {
        JOC_REVERSE_PROXY_URI = normalizeUri(val);
    }

    public String getJocReverseProxyUri() {
        return JOC_REVERSE_PROXY_URI;
    }

    private String normalizeUri(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        return val.endsWith("/") ? val.substring(0, val.length() - 1) : val;
    }

    private String getJocBaseUri() {
        try {
            if (Globals.servletBaseUri != null) {
                String hostname = SOSShell.getHostname();
                String baseUri = Globals.servletBaseUri.normalize().toString().replaceFirst("/joc/api(/.*)?$", "");
                if (baseUri.matches("https?://localhost:.*") && hostname != null) {
                    baseUri = baseUri.replaceFirst("^(https?://)localhost:", "$1" + hostname + ":");
                }
                return baseUri;
            }
        } catch (Throwable e) {

        }
        return "";
    }

    protected void process(String xml) {
        init();

        if (SOSString.isEmpty(xml)) {
            return;
        }

        try {
            Document doc = SOSXML.parse(xml);

            NodeList nl = SOSXML.newXPath().selectNodes(doc, "./Configurations/Notifications/Notification");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    add2type(new Notification(doc, (Element) nl.item(i), (i + 1)));
                }
            }

            Node n = SOSXML.newXPath().selectNode(doc, "./Configurations/Notifications/SystemNotification");
            if (n != null) {
                systemNotification = new SystemNotification(doc, n);
                handleMailResources(systemNotification.getJobResources());
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            exists = false;
        }
    }

    public List<Notification> findWorkflowMatches(NotificationRange range, List<Notification> source, String controllerId, String workflowPath) {
        return findWorkflowMatches(range, source, controllerId, workflowPath, null, null, null, null);
    }

    public List<Notification> findWorkflowMatches(NotificationRange range, List<Notification> source, String controllerId, String workflowPath,
            String jobName, String jobLabel, Integer criticality, Integer returnCode) {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[%s]controllerId=%s,workflowPath=%s,jobName=%s, jobLabel=%s, criticality=%s,returnCode=%s", LOG_FIND_START,
                    range, controllerId, workflowPath, jobName, jobLabel, criticality, returnCode));
        }

        boolean analyzeJobs = jobName != null;
        List<Notification> result = new ArrayList<>();
        for (Notification n : source) {
            x: for (Workflow w : n.getWorkflows()) {
                if (w.getPath().equals(AElement.ASTERISK) || (workflowPath != null && workflowPath.matches(w.getPath()))) {
                    if (w.getControllerId().equals(AElement.ASTERISK) || (controllerId != null && controllerId.matches(w.getControllerId()))) {
                        if (w.getJobs().size() == 0) {
                            if (analyzeJobs) {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "%s[%s][skip][%s][WorkfowJob notification][job=%s]skip because 0 WorkflowJob configured", LOG_FIND_END,
                                            range, toString(n), jobName));
                                }
                            } else {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "%s[%s][found][%s][workflowPath match and 0 WorkflowJob]configured path=%s, controller_id=%s",
                                            LOG_FIND_END, range, toString(n), w.getPath(), w.getControllerId()));
                                }
                                result.add(n);
                            }
                            break x;
                        } else if (analyzeJobs) {
                            for (WorkflowJob j : w.getJobs()) {
                                if (j.getName().equals(AElement.ASTERISK) || jobName.matches(j.getName())) {
                                    if (j.getLabel().equals(AElement.ASTERISK) || (jobLabel != null && jobLabel.matches(j.getLabel()))) {
                                        if (j.getCriticality().equals(CriticalityType.ALL) || j.getCriticality().equals(WorkflowJob.getCriticality(
                                                criticality))) {
                                            if (j.getReturnCodeFrom() == -1 || (returnCode != null && returnCode >= j.getReturnCodeFrom())) {
                                                if (j.getReturnCodeTo() == -1 || returnCode <= j.getReturnCodeTo()) {
                                                    if (isDebugEnabled) {
                                                        LOGGER.debug(String.format(
                                                                "%s[%s][found][%s][job match][configured][workflow path=%s controller_id=%s][job]name=%s, label=%s, criticality=%s, return_code_from=%s, return_code_to=%s",
                                                                LOG_FIND_END, range, toString(n), w.getPath(), w.getControllerId(), j.getName(), j
                                                                        .getLabel(), j.getCriticality(), j.getReturnCodeFrom(), j.getReturnCodeTo()));
                                                    }
                                                    result.add(n);
                                                    break x;
                                                } else if (isDebugEnabled) {
                                                    LOGGER.debug(String.format("%s[%s][skip][%s][returnCodeTo not match]current=%s, configured=%s",
                                                            LOG_FIND_END, range, toString(n), returnCode, j.getReturnCodeTo()));
                                                }
                                            } else if (isDebugEnabled) {
                                                LOGGER.debug(String.format("%s[%s][skip][%s][returnCodeFrom not match]current=%s, configured=%s",
                                                        LOG_FIND_END, range, toString(n), returnCode, j.getReturnCodeFrom()));
                                            }
                                        } else if (isDebugEnabled) {
                                            LOGGER.debug(String.format("%s[%s][skip][%s][criticality not match]current=%s, configured=%s",
                                                    LOG_FIND_END, range, toString(n), WorkflowJob.getCriticality(criticality), j.getCriticality()));
                                        }
                                    } else if (isDebugEnabled) {
                                        LOGGER.debug(String.format("%s[%s][skip][%s][jobLabel not match]current=%s, configured=%s", LOG_FIND_END,
                                                range, toString(n), jobLabel, j.getLabel()));
                                    }
                                } else if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[%s][skip][%s][jobName not match]current=%s, configured=%s", LOG_FIND_END, range,
                                            toString(n), jobName, j.getName()));
                                }
                            }
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[%s][skip][%s][Workfow notification]skip because WorkflowJob configured", LOG_FIND_END,
                                        range, toString(n)));
                            }
                        }
                    }
                } else if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][skip][%s][workflowPath not match]current=%s, configured=%s", LOG_FIND_END, range, toString(n),
                            workflowPath, w.getPath()));
                }
            }
        }
        return result;
    }

    private static String toString(Notification n) {
        StringBuilder sb = new StringBuilder();
        sb.append("notification_id=").append(n.getNotificationId());
        sb.append(",types=").append(n.getTypes());
        return sb.toString();
    }

    private void init() {
        systemNotification = null;
        onError = new ArrayList<>();
        onWarning = new ArrayList<>();
        onSuccess = new ArrayList<>();
        mailResources = new HashMap<>();
        exists = false;
    }

    private void add2type(Notification n) {
        exists = true;

        for (NotificationType nt : n.getTypes()) {
            switch (nt) {
            case ERROR:
                onError.add(n);
                break;
            case WARNING:
                onWarning.add(n);
                break;
            case SUCCESS:
                onSuccess.add(n);
                break;
            default:
                break;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("notification=" + SOSString.toString(n));
        }

        handleMailResources(n.getJobResources());
    }

    private void handleMailResources(List<String> jobResources) {
        for (String res : jobResources) {
            if (!mailResources.containsKey(res)) {
                mailResources.put(res, new MailResource());
            }
        }
    }

    public SystemNotification getSystemNotification() {
        return systemNotification;
    }

    public List<Notification> getOnError() {
        return onError;
    }

    public List<Notification> getOnWarning() {
        return onWarning;
    }

    public List<Notification> getOnSuccess() {
        return onSuccess;
    }

    public Map<String, MailResource> getMailResources() {
        return mailResources;
    }

    public boolean exists() {
        return exists;
    }

    public boolean hasOnError() {
        return onError.size() > 0;
    }

    public boolean hasOnWarning() {
        return onWarning.size() > 0;
    }

    public boolean hasOnSuccess() {
        return onSuccess.size() > 0;
    }
}