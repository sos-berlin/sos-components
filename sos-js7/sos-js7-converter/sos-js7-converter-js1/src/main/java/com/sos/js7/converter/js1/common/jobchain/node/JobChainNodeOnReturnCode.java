package com.sos.js7.converter.js1.common.jobchain.node;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.commands.AddOrder;

public class JobChainNodeOnReturnCode {

    private static final String ELEMENT_TO_STATE = "to_state";
    private static final String ELEMENT_ADD_ORDER = "add_order";

    private static final String ATTR_RETUN_CODE = "return_code";
    private static final String ATTR_STATE = "state";

    private final List<AddOrder> addOrders;
    private final String returnCode;
    private final String toState;

    protected JobChainNodeOnReturnCode(Path jobChainPath, Node node) throws Exception {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        returnCode = JS7ConverterHelper.stringValue(m.get(ATTR_RETUN_CODE));

        SOSXMLXPath xpath = SOSXML.newXPath();
        toState = getToState(xpath, node);
        addOrders = getAddOrders(xpath, node);

    }

    private String getToState(SOSXMLXPath xpath, Node node) throws Exception {
        NodeList nl = xpath.selectNodes(node, "./" + ELEMENT_TO_STATE);
        if (nl != null && nl.getLength() > 0) {
            Map<String, String> m = JS7ConverterHelper.attribute2map(nl.item(0));
            return JS7ConverterHelper.stringValue(m.get(ATTR_STATE));
        }
        return null;
    }

    private List<AddOrder> getAddOrders(SOSXMLXPath xpath, Node node) throws Exception {
        NodeList nl = xpath.selectNodes(node, "./" + ELEMENT_ADD_ORDER);
        if (nl == null || nl.getLength() == 0) {
            return Collections.emptyList();
        }

        List<AddOrder> l = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            l.add(new AddOrder(nl.item(i)));
        }

        return l;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getToState() {
        return toState;
    }

    public List<AddOrder> getAddOrders() {
        return addOrders;
    }
}
