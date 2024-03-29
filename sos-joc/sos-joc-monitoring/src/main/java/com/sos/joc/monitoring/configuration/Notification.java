package com.sos.joc.monitoring.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.configuration.monitor.MonitorCommand;
import com.sos.joc.monitoring.configuration.monitor.MonitorNSCA;
import com.sos.joc.monitoring.configuration.monitor.jms.MonitorJMS;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;
import com.sos.joc.monitoring.configuration.objects.workflow.Workflow;
import com.sos.joc.monitoring.exception.SOSMissingChildElementsException;
import com.sos.monitoring.notification.NotificationType;

public class Notification extends AElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notification.class);

    private static final String ELEMENT_NAME_NOTIFICATION_MONITORS = "NotificationMonitors";
    private static final String ELEMENT_NAME_NOTIFICATION_OBJECTS = "NotificationObjects";

    private static final String ELEMENT_NAME_WORKFLOW = "Workflow";

    private static final String ELEMENT_NAME_COMMAND_FRAGMENT_REF = "CommandFragmentRef";
    private static final String ELEMENT_NAME_MAIL_FRAGMENT_REF = "MailFragmentRef";
    private static final String ELEMENT_NAME_JMS_FRAGMENT_REF = "JMSFragmentRef";
    private static final String ELEMENT_NAME_NSCA_FRAGMENT_REF = "NSCAFragmentRef";

    private static final String ELEMENT_NAME_WORKFLOWS_REF = "WorkflowsRef";

    private static String ATTRIBUTE_NAME_NOTIFICATION_ID = "notification_id";
    private static String ATTRIBUTE_NAME_TYPE = "type";
    private static String ATTRIBUTE_NAME_CONTROLLER_ID = "controller_id";

    private final List<NotificationType> types;
    private final List<AMonitor> monitors;
    private final List<Workflow> workflows;
    private final List<String> jobResources;
    private final String notificationId;

    public Notification(Document doc, Node node, int position) throws Exception {
        super(node);
        this.types = evaluateTypes();
        this.notificationId = getAttributeValue(ATTRIBUTE_NAME_NOTIFICATION_ID, String.valueOf(position));
        this.monitors = new ArrayList<>();
        this.workflows = new ArrayList<>();
        this.jobResources = new ArrayList<>();
        process(doc);
    }

    private void process(Document doc) throws Exception {
        List<Element> children = SOSXML.getChildElemens(getElement());
        if (children == null) {
            throw new SOSMissingChildElementsException(getElementName());
        }
        for (Element child : children) {
            switch (child.getNodeName()) {
            case ELEMENT_NAME_NOTIFICATION_MONITORS:
                List<Element> elements = SOSXML.getChildElemens(child);
                if (elements == null) {
                    throw new SOSMissingChildElementsException(getElementName() + "/" + child.getNodeName());
                }
                for (Element monitor : elements) {
                    try {
                        switch (monitor.getNodeName()) {
                        case ELEMENT_NAME_COMMAND_FRAGMENT_REF:
                            monitors.add(new MonitorCommand(doc, monitor, notificationId));
                            break;
                        case ELEMENT_NAME_MAIL_FRAGMENT_REF:
                            MonitorMail mm = new MonitorMail(doc, monitor, notificationId);
                            monitors.add(mm);
                            if (mm.getJobResources() != null) {
                                for (String r : mm.getJobResources()) {
                                    if (!jobResources.contains(r)) {
                                        jobResources.add(r);
                                    }
                                }
                            }
                            break;
                        case ELEMENT_NAME_NSCA_FRAGMENT_REF:
                            monitors.add(new MonitorNSCA(doc, monitor, notificationId));
                            break;
                        case ELEMENT_NAME_JMS_FRAGMENT_REF:
                            monitors.add(new MonitorJMS(doc, monitor, notificationId));
                            break;
                        }
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s ref=%s][skip]%s", monitor.getNodeName(), monitor.getAttribute(AMonitor.ATTRIBUTE_NAME_REF), e
                                .toString()), e);
                    }
                }
                break;
            case ELEMENT_NAME_NOTIFICATION_OBJECTS:
                handleWorkflows(doc, child);
                break;
            }
        }
    }

    private void handleWorkflows(Document doc, Element notificationObjects) throws Exception {
        List<Element> elements = SOSXML.getChildElemens(notificationObjects, ELEMENT_NAME_WORKFLOWS_REF);
        if (elements == null) {
            throw new SOSMissingChildElementsException(getElementName() + "/" + notificationObjects.getNodeName());
        }
        List<String> refs = new ArrayList<>();
        for (Element object : elements) {
            String ref = object.getAttribute(AMonitor.ATTRIBUTE_NAME_REF);
            if (!SOSString.isEmpty(ref)) {
                if (!refs.contains(ref)) {
                    Node n = resolveWorkflowRef(doc, ref);
                    if (n != null) {
                        String controllerId = getAttributeValue(n, ATTRIBUTE_NAME_CONTROLLER_ID, AElement.ASTERISK);
                        List<Element> ws = SOSXML.getChildElemens(n, ELEMENT_NAME_WORKFLOW);
                        if (ws != null) {
                            for (Element ew : ws) {
                                workflows.add(new Workflow(ew, controllerId));
                            }
                        }
                    }
                    refs.add(ref);
                }
            }
        }
    }

    private Node resolveWorkflowRef(Document document, String ref) throws SOSXMLXPathException {
        return (Node) SOSXML.newXPath().selectNode(document.getDocumentElement(), "./Fragments/ObjectFragments/Workflows[@name='" + ref + "']");
    }

    private List<NotificationType> evaluateTypes() {
        List<NotificationType> result = new ArrayList<>();
        try {
            String[] values = getElement().getAttribute(ATTRIBUTE_NAME_TYPE).split(" ");
            for (String val : values) {
                if (val.trim().length() == 0) {
                    continue;
                }
                NotificationType nt = NotificationType.valueOf(val);
                if (!result.contains(nt)) {
                    result.add(nt);
                }
            }
        } catch (Throwable e) {
            result = new ArrayList<>();
            result.add(NotificationType.ERROR);
            result.add(NotificationType.WARNING);
        }
        return result;
    }

    public List<NotificationType> getTypes() {
        return types;
    }

    public List<AMonitor> getMonitors() {
        return monitors;
    }

    public String getMonitorsAsString() {
        if (monitors == null || monitors.size() == 0) {
            return "";
        }
        return monitors.stream().map(n -> n.getClass().getSimpleName()).collect(Collectors.joining(","));
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public String getNotificationId() {
        return notificationId;
    }

    protected List<String> getJobResources() {
        return jobResources;
    }
}
