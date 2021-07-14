package com.sos.joc.monitoring.configuration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;

public abstract class AElement {

    public static final String ASTERISK = "*";

    private final Element element;
    private final String elementName;

    public AElement(Node node) {
        element = (Element) node;
        elementName = element.getNodeName();
    }

    public Element getElement() {
        return element;
    }

    public String getElementName() {
        return elementName;
    }

    protected String getAttributeValue(String attr) {
        return getValue(element.getAttribute(attr));
    }

    protected String getAttributeValue(String attr, String defaultValue) {
        return getValue(element.getAttribute(attr), defaultValue);
    }

    protected String getAttributeValue(Node node, String attr, String defaultValue) {
        return getValue(((Element) node).getAttribute(attr), defaultValue);
    }

    protected static String getValue(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        return val.trim();
    }

    protected static String getValue(String val, String defaultValue) {
        if (SOSString.isEmpty(val)) {
            return defaultValue;
        }
        return val.trim();
    }

    protected static int getValue(String val, int defaultValue) {
        if (SOSString.isEmpty(val)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected static long getValue(String val, long defaultValue) {
        if (SOSString.isEmpty(val)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
