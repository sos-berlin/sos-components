package com.sos.joc.monitoring.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.configuration.monitor.MonitorCommand;
import com.sos.joc.monitoring.configuration.monitor.MonitorNSCA;
import com.sos.joc.monitoring.configuration.monitor.jms.MonitorJMS;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;
import com.sos.joc.monitoring.exception.SOSMissingChildElementsException;
import com.sos.monitoring.notification.NotificationType;

public class SystemNotification extends AElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotification.class);

    public static final String NOTIFICATION_ID = "joc_system_notification";

    private static final String ELEMENT_NAME_NOTIFICATION_MONITORS = "NotificationMonitors";

    private static final String ELEMENT_NAME_COMMAND_FRAGMENT_REF = "CommandFragmentRef";
    private static final String ELEMENT_NAME_MAIL_FRAGMENT_REF = "MailFragmentRef";
    private static final String ELEMENT_NAME_JMS_FRAGMENT_REF = "JMSFragmentRef";
    private static final String ELEMENT_NAME_NSCA_FRAGMENT_REF = "NSCAFragmentRef";

    private static String ATTRIBUTE_NAME_TYPE = "type";

    private final List<NotificationType> types;
    private final List<AMonitor> monitors;
    private final List<String> jobResources;

    public SystemNotification(Document doc, Node node) throws Exception {
        super(node);
        this.types = evaluateTypes();
        this.monitors = new ArrayList<>();
        this.jobResources = new ArrayList<>();
        process(doc);
    }

    private SystemNotification(Node element, List<NotificationType> types, List<AMonitor> monitors, List<String> jobResources) {
        super(element);
        this.types = new ArrayList<>(types);
        this.monitors = new ArrayList<>(monitors);
        this.jobResources = new ArrayList<>(jobResources);
    }

    public SystemNotification clone() {
        return new SystemNotification(this.getElement(), this.types, this.monitors, this.jobResources);
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
                            monitors.add(new MonitorCommand(doc, monitor, NOTIFICATION_ID));
                            break;
                        case ELEMENT_NAME_MAIL_FRAGMENT_REF:
                            MonitorMail mm = new MonitorMail(doc, monitor, NOTIFICATION_ID);
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
                            monitors.add(new MonitorNSCA(doc, monitor, NOTIFICATION_ID));
                            break;
                        case ELEMENT_NAME_JMS_FRAGMENT_REF:
                            monitors.add(new MonitorJMS(doc, monitor, NOTIFICATION_ID));
                            break;
                        }
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s ref=%s][skip]%s", monitor.getNodeName(), monitor.getAttribute(AMonitor.ATTRIBUTE_NAME_REF), e
                                .toString()), e);
                    }
                }
                break;
            }
        }
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

    public String getTypesAsString() {
        if (types == null || types.size() == 0) {
            return "";
        }
        return types.stream().map(n -> n.name()).collect(Collectors.joining(","));
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

    protected List<String> getJobResources() {
        return jobResources;
    }
}
