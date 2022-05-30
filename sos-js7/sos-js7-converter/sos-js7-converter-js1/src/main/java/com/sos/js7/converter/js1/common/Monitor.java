package com.sos.js7.converter.js1.common;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Monitor {

    private static final String ATTR_NAME = "name";
    private static final String ATTR_ORDERING = "ordering";
    private static final String ELEMENT_SCRIPT = "script";

    private Script script;

    private String name;
    private Integer ordering;

    public Monitor(SOSXMLXPath xpath, Node node) throws Exception {
        Node script = xpath.selectNode(node, "./" + ELEMENT_SCRIPT);
        if (script != null) {
            this.script = new Script(xpath, script);
        }

        Map<String, String> attributes = JS7ConverterHelper.attribute2map(node);
        name = JS7ConverterHelper.stringValue(attributes.get(ATTR_NAME));
        ordering = JS7ConverterHelper.integerValue(attributes.get(ATTR_ORDERING));
    }

    public Script getScript() {
        return script;
    }

    public String getName() {
        return name;
    }

    public Integer getOrdering() {
        return ordering;
    }
}
