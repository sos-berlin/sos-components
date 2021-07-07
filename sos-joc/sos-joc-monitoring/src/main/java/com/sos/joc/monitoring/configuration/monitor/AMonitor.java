package com.sos.joc.monitoring.configuration.monitor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.joc.monitoring.configuration.AElement;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.notification.notifier.ANotifier;

public abstract class AMonitor extends AElement {

    private static final String ELEMENT_NAME_MESSAGE_REF = "MessageRef";
    private static final String ATTRIBUTE_NAME_NAME = "name";

    private final String refElementName;
    private final String monitorName;

    private Element refElement;
    private String message;

    public AMonitor(Document document, Node node) throws Exception {
        super(node);
        // CommandFragmentRef (see above) -> CommandFragment
        refElementName = getElementName().substring(0, getElementName().length() - 3);
        resolveRefs(document);

        if (refElement == null) {
            String ref = getElement().getAttribute("ref");
            throw new Exception(String.format("[%s ref=\"%s\"]missing refNode \"%s[@name=" + ref + "]\"", getElementName(), ref, refElementName,
                    ref));
        }
        if (message == null) {
            throw new Exception(String.format("[%s ref=\"%s\"]missing message", getElementName(), getElement().getAttribute("ref"), refElementName));
        }
        monitorName = getValue(refElement.getAttribute(ATTRIBUTE_NAME_NAME));
    }

    public abstract ANotifier createNotifier(Configuration conf);

    private void resolveRefs(Document document) throws SOSXMLXPathException {
        SOSXMLXPath xpath = SOSXML.newXPath();

        setRefElement(document, xpath);
        if (refElement == null) {
            return;
        }
        setMessage(document, xpath, getElement());
        if (message == null) {
            setMessage(document, xpath, refElement);
        }
    }

    private void setRefElement(Document document, SOSXMLXPath xpath) throws SOSXMLXPathException {
        refElement = (Element) xpath.selectNode(document.getDocumentElement(), "./Fragments/MonitorFragments/" + refElementName + "[@name='"
                + getElement().getAttribute("ref") + "']");
    }

    private void setMessage(Document document, SOSXMLXPath xpath, Element element) throws SOSXMLXPathException {
        Element msg = (Element) SOSXML.getChildNode(element, ELEMENT_NAME_MESSAGE_REF);
        if (msg != null) {
            Node refMsg = xpath.selectNode(document.getDocumentElement(), "./Fragments/MessageFragments/Message[@name='" + msg.getAttribute("ref")
                    + "']");
            if (refMsg != null) {
                message = SOSXML.getTrimmedValue(refMsg);
            }
        }
    }

    protected Element getRefElement() {
        return refElement;
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
