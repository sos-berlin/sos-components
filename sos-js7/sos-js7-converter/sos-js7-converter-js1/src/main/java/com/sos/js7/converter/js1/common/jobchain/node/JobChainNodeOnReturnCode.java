package com.sos.js7.converter.js1.common.jobchain.node;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNodeOnReturnCode {

    private static final String ELEMENT_TO_STATE = "to_state";

    private static final String ATTR_RETUN_CODE = "return_code";
    private static final String ATTR_STATE = "state";

    private final String returnCode;
    private final String toState;

    protected JobChainNodeOnReturnCode(Path path, Node node) throws Exception {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        returnCode = JS7ConverterHelper.stringValue(m.get(ATTR_RETUN_CODE));
        toState = getToState(path, node);
        if (returnCode == null) {
            // throw new Exception("missing " + ATTR_RETUN_CODE);
        }
        if (toState == null) {
            // throw new Exception("missing " + ELEMENT_TO_STATE + "/" + ATTR_STATE);
        }
    }

    private String getToState(Path path, Node node) throws Exception {
        SOSXMLXPath xpath = SOSXML.newXPath();
        NodeList nl = xpath.selectNodes(node, "./" + ELEMENT_TO_STATE);
        if (nl != null && nl.getLength() > 0) {
            Map<String, String> m = JS7ConverterHelper.attribute2map(nl.item(0));
            return JS7ConverterHelper.stringValue(m.get(ATTR_STATE));
        }
        return null;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getToState() {
        return toState;
    }
}
