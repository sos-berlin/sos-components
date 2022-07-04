package com.sos.joc.monitoring.configuration.monitor;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.joc.monitoring.configuration.AElement;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.monitoring.MonitorType;

public abstract class AMonitor extends AElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(AMonitor.class);

    public static final String ATTRIBUTE_NAME_REF = "ref";

    private static final String ELEMENT_NAME_MESSAGE_REF = "MessageRef";
    private static final String ATTRIBUTE_NAME_NAME = "name";
    private static final String ATTRIBUTE_NAME_TIME_ZONE = "time_zone";
    private static final String DEFAULT_TIME_ZONE = "Etc/UTC";

    private final String notificationId;
    private final String refElementName;
    private final String monitorName;

    private Element refElement;
    private String ref;
    private TimeZone timeZone;
    private String message;

    public AMonitor(Document document, Node node, String notificationId) throws Exception {
        super(node);
        this.notificationId = notificationId;
        // CommandFragmentRef (see above) -> CommandFragment
        this.refElementName = getElementName().substring(0, getElementName().length() - 3);
        resolveRefs(document, node);

        if (this.refElement == null) {
            this.ref = getElement().getAttribute(ATTRIBUTE_NAME_REF);
            throw new Exception(String.format("[%s %s=\"%s\"]missing refNode \"%s[@name=" + ref + "]\"", getElementName(), ATTRIBUTE_NAME_REF,
                    this.ref, this.refElementName, this.ref));
        }
        if (this.message == null) {
            throw new Exception(String.format("[%s %s=\"%s\"]missing message", getElementName(), ATTRIBUTE_NAME_REF, getElement().getAttribute(
                    ATTRIBUTE_NAME_REF), this.refElementName));
        }
        this.monitorName = getValue(this.refElement.getAttribute(ATTRIBUTE_NAME_NAME));
    }

    public abstract ANotifier createNotifier(int nr, Configuration conf) throws Exception;

    public abstract MonitorType getType();

    private void resolveRefs(Document document, Node monitorRefNode) throws SOSXMLXPathException {
        SOSXMLXPath xpath = SOSXML.newXPath();

        setRefElement(document, xpath);
        if (refElement == null) {
            return;
        }
        Node messageNode = setMessage(document, xpath, getElement());
        if (message == null) {
            messageNode = setMessage(document, xpath, refElement);
        }

        setTimeZone(document, xpath, monitorRefNode, messageNode);
    }

    private void setRefElement(Document document, SOSXMLXPath xpath) throws SOSXMLXPathException {
        ref = getElement().getAttribute(ATTRIBUTE_NAME_REF);
        refElement = (Element) xpath.selectNode(document.getDocumentElement(), "./Fragments/MonitorFragments/" + refElementName + "[@name='" + ref
                + "']");
    }

    private Node setMessage(Document document, SOSXMLXPath xpath, Element element) throws SOSXMLXPathException {
        Node messageNode = null;
        Element msg = (Element) SOSXML.getChildNode(element, ELEMENT_NAME_MESSAGE_REF);
        if (msg != null) {
            messageNode = xpath.selectNode(document.getDocumentElement(), "./Fragments/MessageFragments/Message[@name='" + msg.getAttribute("ref")
                    + "']");
            if (messageNode != null) {
                message = SOSXML.getTrimmedValue(messageNode);
            }
        }
        return messageNode;
    }

    private void setTimeZone(Document document, SOSXMLXPath xpath, Node monitorRefNode, Node messageNode) throws SOSXMLXPathException {
        // 1) Notifications/Notification/NotificationMonistors/<Command|Mail|...>FragmentRef
        String tzPath = "Notifications/Notification notification_id=" + notificationId + "/NotificationMonistors/" + getElementName() + " ref=" + ref;
        String tz = SOSXML.getAttributeValue(monitorRefNode, ATTRIBUTE_NAME_TIME_ZONE);
        if (SOSString.isEmpty(tz)) {
            if (messageNode != null) {
                // 2 - Fragments/MessageFragments/Message ...
                tzPath = "Fragments/MessageFragments/Message name=" + SOSXML.getAttributeValue(messageNode, "name");
                tz = SOSXML.getAttributeValue(messageNode, ATTRIBUTE_NAME_TIME_ZONE);
                if (SOSString.isEmpty(tz)) {
                    // 3 - Fragments/MessageFragments
                    Node mf = xpath.selectNode(document.getDocumentElement(), "./Fragments/MessageFragments");
                    if (mf != null) {
                        tzPath = "Fragments/MessageFragments";
                        tz = SOSXML.getAttributeValue(mf, ATTRIBUTE_NAME_TIME_ZONE);
                    }
                }
            }
        }

        if (!SOSString.isEmpty(tz)) {
            TimeZone timeZone = TimeZone.getTimeZone(tz);
            if (timeZone.getID().equals("GMT") && !tz.toUpperCase().contains("GMT")) {
                LOGGER.warn(String.format("[monitor][config][%s]The given time_zone=%s cannot be understood. Use default=%s", tzPath, tz,
                        DEFAULT_TIME_ZONE));
            } else {
                this.timeZone = timeZone;
            }
        }
    }

    public StringBuilder getInfo() {
        StringBuilder sb = new StringBuilder(refElementName);
        sb.append(" name=").append(monitorName);
        sb.append(",timeZone=").append(getTimeZoneValue());
        return sb;
    }

    public String getTimeZoneValue() {
        return timeZone == null ? DEFAULT_TIME_ZONE : timeZone.getID();
    }

    protected Element getRefElement() {
        return refElement;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getMessage() {
        return message;
    }

    public String getRefElementName() {
        return refElementName;
    }

    public String getMonitorName() {
        return monitorName;
    }
}
