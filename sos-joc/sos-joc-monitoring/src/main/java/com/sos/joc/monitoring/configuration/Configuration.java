package com.sos.joc.monitoring.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.configuration.objects.workflow.Workflow;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob.CriticalityType;
import com.sos.monitoring.notification.NotificationType;

public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static String JOC_URI;

    private List<Notification> onError;
    private List<Notification> onWarning;
    private List<Notification> onSuccess;
    private Map<String, MailResource> mailResources;

    private boolean exists;

    public Configuration(String jocUri) {
        JOC_URI = jocUri;
    }

    public static String getJocUri() {
        if (SOSString.isEmpty(JOC_URI)) {
            JOC_URI = getJocBaseUri();
        }
        return JOC_URI;
    }

    private static String getJocBaseUri() {
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

    public void process(String xml) {
        init();

        if (SOSString.isEmpty(xml)) {
            return;
        }

        Document doc;
        try {
            doc = SOSXML.parse(xml);

            NodeList notifications = SOSXML.newXPath().selectNodes(doc, "./Configurations/Notifications/Notification");
            if (notifications != null) {
                for (int i = 0; i < notifications.getLength(); i++) {
                    add2type(new Notification(doc, (Element) notifications.item(i), (i + 1)));
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            exists = false;
        }
    }

    public List<Notification> findWorkflowMatches(List<Notification> source, String controllerId, String workflowPath) {
        return findWorkflowMatches(source, controllerId, workflowPath, null, null, null, null, true);
    }

    public List<Notification> findWorkflowMatches(List<Notification> source, String controllerId, String workflowPath, String jobName,
            String jobLabel, Integer criticality, Integer returnCode) {
        return findWorkflowMatches(source, controllerId, workflowPath, jobName, jobLabel, criticality, returnCode, false);
    }

    public List<Notification> findWorkflowMatches(List<Notification> source, String controllerId, String workflowPath, String jobName,
            String jobLabel, Integer criticality, Integer returnCode, boolean includeEmptyWorkflows) {

        boolean debug = LOGGER.isDebugEnabled();
        if (debug) {
            LOGGER.debug(String.format("[find]controllerId=%s,workflowPath=%s,jobName=%s, jobLabel=%s, criticality=%s,returnCode=%s", controllerId,
                    workflowPath, jobName, jobLabel, criticality, returnCode));
        }

        boolean analyzeJobs = jobName != null;
        List<Notification> result = new ArrayList<>();
        for (Notification n : source) {
            x: for (Workflow w : n.getWorkflows()) {
                if (w.getPath().equals(AElement.ASTERISK) || (workflowPath != null && workflowPath.matches(w.getPath()))) {
                    if (w.getControllerId().equals(AElement.ASTERISK) || (controllerId != null && controllerId.matches(w.getControllerId()))) {
                        if (w.getJobs().size() == 0) {
                            if (debug) {
                                LOGGER.debug(String.format("[find][found][%s][workflowPath match and 0 jobs]configured path=%s, controller_id=%s",
                                        toString(n), w.getPath(), w.getControllerId()));
                            }
                            if (includeEmptyWorkflows) {
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
                                                    LOGGER.debug(String.format(
                                                            "[find][found][%s][job match][configured][workflow path=%s controller_id=%s][job]name=%s, label=%s, criticality=%s, return_code_from=%s, return_code_to=%s",
                                                            toString(n), w.getPath(), w.getControllerId(), j.getName(), j.getLabel(), j
                                                                    .getCriticality(), j.getReturnCodeFrom(), j.getReturnCodeTo()));
                                                    result.add(n);
                                                    break x;
                                                } else if (debug) {
                                                    LOGGER.debug(String.format("[find][skip][%s][returnCodeTo not match]current=%s, configured=%s",
                                                            toString(n), returnCode, j.getReturnCodeTo()));
                                                }
                                            } else if (debug) {
                                                LOGGER.debug(String.format("[find][skip][%s][returnCodeFrom not match]current=%s, configured=%s",
                                                        toString(n), returnCode, j.getReturnCodeFrom()));
                                            }
                                        } else if (debug) {
                                            LOGGER.debug(String.format("[find][skip][%s][criticality not match]current=%s, configured=%s", toString(
                                                    n), WorkflowJob.getCriticality(criticality), j.getCriticality()));
                                        }
                                    } else if (debug) {
                                        LOGGER.debug(String.format("[find][skip][%s][jobLabel not match]current=%s, configured=%s", toString(n),
                                                jobLabel, j.getLabel()));
                                    }
                                } else if (debug) {
                                    LOGGER.debug(String.format("[find][skip][%s][jobName not match]current=%s, configured=%s", toString(n), jobName, j
                                            .getName(), toString(n)));
                                }
                            }
                        } else if (debug) {
                            LOGGER.debug(String.format("[find][skip][%s][wrong method usage][workflow current=%s, configured=%s]%s jobs configured",
                                    toString(n), workflowPath, w.getPath(), w.getJobs().size()));
                        }
                    } else if (debug) {
                        LOGGER.debug(String.format("[find][skip][%s][controllerId not match]current=%s, configured=%s", toString(n), controllerId, w
                                .getControllerId()));

                    }
                } else if (debug) {
                    LOGGER.debug(String.format("[find][skip][%s][workflowPath not match]current=%s, configured=%s", toString(n), workflowPath, w
                            .getPath()));
                }
            }
        }
        return result;
    }

    private String toString(Notification n) {
        StringBuilder sb = new StringBuilder("notification ");
        sb.append("type=").append(n.getTypes());
        sb.append(", notification_id=");
        sb.append(n.getNotificationId());
        return sb.toString();
    }

    private void init() {
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
