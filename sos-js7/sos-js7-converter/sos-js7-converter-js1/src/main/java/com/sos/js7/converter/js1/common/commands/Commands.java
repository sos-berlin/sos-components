package com.sos.js7.converter.js1.common.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Commands {

    private static final String ELEMENT_ADD_ORDER = "add_order";
    private static final String ELEMENT_ORDER = "order";

    private static final String ATTR_ON_EXIT_CODE = "on_exit_code";

    private final String onExitCode;
    private final List<AddOrder> addOrders;
    private final List<Order> orders;

    public Commands(Node node) throws Exception {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        onExitCode = JS7ConverterHelper.stringValue(m.get(ATTR_ON_EXIT_CODE));

        SOSXMLXPath xpath = SOSXML.newXPath();
        addOrders = getAddOrders(xpath, node);
        orders = getOrders(xpath, node);
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

    private List<Order> getOrders(SOSXMLXPath xpath, Node node) throws Exception {
        NodeList nl = xpath.selectNodes(node, "./" + ELEMENT_ORDER);
        if (nl == null || nl.getLength() == 0) {
            return Collections.emptyList();
        }

        List<Order> l = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            l.add(new Order(nl.item(i)));
        }

        return l;
    }

    public String getOnExitCode() {
        return onExitCode;
    }

    public List<AddOrder> getAddOrders() {
        return addOrders;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
