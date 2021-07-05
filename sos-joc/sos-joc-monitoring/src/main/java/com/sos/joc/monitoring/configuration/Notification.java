package com.sos.joc.monitoring.configuration;

import java.util.ArrayList;
import java.util.List;

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

public class Notification extends AElement {

    public enum NotificationType {
        ALL, ON_ERROR, ON_SUCCESS
    }

    private static final String ELEMENT_NAME_NOTIFICATION_MONITORS = "NotificationMonitors";
    private static final String ELEMENT_NAME_NOTIFICATION_OBJECTS = "NotificationObjects";

    private static final String ELEMENT_NAME_WORKFLOW = "Workflow";

    private static final String ELEMENT_NAME_COMMAND_FRAGMENT_REF = "CommandFragmentRef";
    private static final String ELEMENT_NAME_MAIL_FRAGMENT_REF = "MailFragmentRef";
    private static final String ELEMENT_NAME_JMS_FRAGMENT_REF = "JMSFragmentRef";
    private static final String ELEMENT_NAME_NSCA_FRAGMENT_REF = "NSCAFragmentRef";

    private static final String ELEMENT_NAME_WORKFLOWS_REF = "WorkflowsRef";

    private static String ATTRIBUTE_NAME_NAME = "name";
    private static String ATTRIBUTE_NAME_TYPE = "type";

    private final NotificationType type;
    private final List<AMonitor> monitors;
    private final List<Workflow> workflows;
    private final List<String> jobResources;
    private final String name;

    public Notification(Document doc, Node node) throws Exception {
        super(node);
        this.type = evaluateType();
        this.name = getAttributeValue(ATTRIBUTE_NAME_NAME);
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
                    switch (monitor.getNodeName()) {
                    case ELEMENT_NAME_COMMAND_FRAGMENT_REF:
                        monitors.add(new MonitorCommand(doc, monitor));
                        break;
                    case ELEMENT_NAME_MAIL_FRAGMENT_REF:
                        MonitorMail mm = new MonitorMail(doc, monitor);
                        monitors.add(mm);
                        if (!SOSString.isEmpty(mm.getJobResource()) && !jobResources.contains(mm.getJobResource())) {
                            jobResources.add(mm.getJobResource());
                        }
                        break;
                    case ELEMENT_NAME_NSCA_FRAGMENT_REF:
                        monitors.add(new MonitorNSCA(doc, monitor));
                        break;
                    case ELEMENT_NAME_JMS_FRAGMENT_REF:
                        monitors.add(new MonitorJMS(doc, monitor));
                        break;
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
        Workflow globalWorkflow = null;

        addWorkflows: for (Element object : elements) {
            String ref = object.getAttribute("ref");
            if (!SOSString.isEmpty(ref)) {
                if (!refs.contains(ref)) {
                    Node n = resolveWorkflowRef(doc, ref);
                    if (n != null) {
                        List<Element> ws = SOSXML.getChildElemens(n, ELEMENT_NAME_WORKFLOW);
                        if (ws != null) {
                            for (Element ew : ws) {
                                Workflow w = new Workflow(ew);
                                if (w.isGlobal()) {
                                    globalWorkflow = w;
                                    break addWorkflows;
                                }
                                workflows.add(w);
                            }
                        }
                    }
                    refs.add(ref);
                }
            }
        }
        if (globalWorkflow != null) {
            workflows.clear();
            workflows.add(globalWorkflow);
        }
    }

    private Node resolveWorkflowRef(Document document, String ref) throws SOSXMLXPathException {
        return (Node) SOSXML.newXPath().selectNode(document.getDocumentElement(), "./Fragments/ObjectsFragments/Workflows[@name='" + ref + "']");
    }

    private NotificationType evaluateType() {
        try {
            return NotificationType.valueOf(getElement().getAttribute(ATTRIBUTE_NAME_TYPE));
        } catch (Throwable e) {
            return NotificationType.ON_ERROR;
        }
    }

    public NotificationType getType() {
        return type;
    }

    public List<AMonitor> getMonitors() {
        return monitors;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public String getName() {
        return name;
    }

    protected List<String> getJobResources() {
        return jobResources;
    }
}
