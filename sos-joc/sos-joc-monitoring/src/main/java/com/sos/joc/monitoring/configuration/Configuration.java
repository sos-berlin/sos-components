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

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.Notification.NotificationType;
import com.sos.joc.monitoring.configuration.monitor.mail.MailResource;
import com.sos.joc.monitoring.configuration.objects.workflow.Workflow;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob.CriticalityType;

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
        return JOC_URI;
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
                    add2type(new Notification(doc, (Element) notifications.item(i)));
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            exists = false;
        }
    }

    public List<Notification> findWorkflowMatches(List<Notification> source, String controllerId, String workflowName) {
        return findWorkflowMatches(source, controllerId, workflowName, null, null, null, null, true);
    }

    public List<Notification> findWorkflowMatches(List<Notification> source, String controllerId, String workflowName, String jobName,
            String jobLabel, Integer criticality, Integer returnCode) {
        return findWorkflowMatches(source, controllerId, workflowName, jobName, jobLabel, criticality, returnCode, false);
    }

    public List<Notification> findWorkflowMatches(List<Notification> source, String controllerId, String workflowName, String jobName,
            String jobLabel, Integer criticality, Integer returnCode, boolean includeEmptyWorkflows) {

        boolean debug = LOGGER.isDebugEnabled();
        if (debug) {
            LOGGER.debug(String.format("[find]controllerId=%s,workflowName=%s,jobName=%s, jobLabel=%s, criticality=%s,returnCode=%s", controllerId,
                    workflowName, jobName, jobLabel, criticality, returnCode));
        }

        boolean analyzeJobs = jobName != null;
        List<Notification> result = new ArrayList<>();
        for (Notification n : source) {
            x: for (Workflow w : n.getWorkflows()) {
                if (w.getName().equals(AElement.ASTERISK) || w.getName().matches(workflowName)) {
                    if (w.getControllerId().equals(AElement.ASTERISK) || w.getControllerId().matches(controllerId)) {
                        if (w.getJobs().size() == 0) {
                            if (debug) {
                                LOGGER.debug(String.format("[find][found][%s]workflowName=%s match and 0 jobs", toString(n), w.getName()));
                            }
                            if (includeEmptyWorkflows) {
                                result.add(n);
                            }
                            break x;
                        } else if (analyzeJobs) {
                            for (WorkflowJob j : w.getJobs()) {
                                if (j.getName().equals(AElement.ASTERISK) || j.getName().matches(jobName)) {
                                    if (j.getLabel().equals(AElement.ASTERISK) || j.getLabel().matches(jobLabel)) {
                                        if (j.getCriticality().equals(CriticalityType.ALL) || j.getCriticality().equals(WorkflowJob.getCriticality(
                                                criticality))) {
                                            if (j.getReturnCodeFrom() == -1 || returnCode >= j.getReturnCodeFrom()) {
                                                if (j.getReturnCodeTo() == -1 || returnCode <= j.getReturnCodeTo()) {
                                                    LOGGER.debug(String.format("[find][found][%s]workflowName=%s, job match", toString(n), w
                                                            .getName()));
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
                        }
                    } else if (debug) {
                        LOGGER.debug(String.format("[find][skip][%s][controllerId not match]current=%s, configured=%s", toString(n), controllerId, w
                                .getControllerId()));

                    }
                } else if (debug) {
                    LOGGER.debug(String.format("[find][skip][%s][workflowName not match]current=%s, configured=%s", toString(n), workflowName, w
                            .getName()));
                }
            }
        }
        return result;
    }

    private String toString(Notification n) {
        StringBuilder sb = new StringBuilder("notification ");
        sb.append("type=").append(n.getTypes());
        sb.append(", name=");
        sb.append(SOSString.isEmpty(n.getName()) ? "<empty>" : n.getName());
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
            case ON_ERROR:
                onError.add(n);
                break;
            case ON_WARNING:
                onWarning.add(n);
                break;
            case ON_SUCCESS:
                onSuccess.add(n);
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
