package com.sos.js7.converter.js1.common.processclass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class ProcessClasses {

    private static final String ATTR_IGNORE = "ignore";
    private static final String ELEMENT_PROCESS_CLASS = "process_class";

    private List<ProcessClass> processClasses;

    private boolean ignore; // yes|no (Initial value: no)

    public ProcessClasses(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        this.ignore = JS7ConverterHelper.booleanValue(map.get(ATTR_IGNORE), false);

        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_PROCESS_CLASS);
        if (l != null && l.getLength() > 0) {
            this.processClasses = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                //this.processClasses.add(new ProcessClass(xpath, l.item(i)));
            }
        }
    }

    public List<ProcessClass> getProcessClasses() {
        return processClasses;
    }

    public boolean isIgnore() {
        return ignore;
    }

}
