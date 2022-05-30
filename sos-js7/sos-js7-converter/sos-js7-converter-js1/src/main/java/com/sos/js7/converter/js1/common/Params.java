package com.sos.js7.converter.js1.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Params {

    private static final String ELEMENT_INCLUDE = "include";
    private static final String ELEMENT_PARAM = "param";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_VALUE = "value";

    private final List<Include> includes;
    private final Map<String, String> params;

    public Params(SOSXMLXPath xpath, Node paramsNode) throws Exception {
        this.includes = getIncludes(xpath, paramsNode);
        this.params = getParams(xpath, paramsNode);
    }

    private List<Include> getIncludes(SOSXMLXPath xpath, Node paramsNode) throws Exception {
        NodeList nl = xpath.selectNodes(paramsNode, "./" + ELEMENT_INCLUDE);
        if (nl == null || nl.getLength() == 0) {
            return Collections.emptyList();
        }
        List<Include> l = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            l.add(new Include(xpath, nl.item(i)));
        }
        return l;
    }

    private Map<String, String> getParams(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        NodeList nl = xpath.selectNodes(node, "./" + ELEMENT_PARAM);
        if (nl == null || nl.getLength() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < nl.getLength(); i++) {
            NamedNodeMap m = nl.item(i).getAttributes();
            map.put(JS7ConverterHelper.getAttributeValue(m, ATTR_NAME), JS7ConverterHelper.getAttributeValue(m, ATTR_VALUE));
        }
        return map;
    }

    public boolean hasParams() {
        return includes.size() > 0 || params.size() > 0;
    }

    public List<Include> getIncludes() {
        return includes;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("params=").append(params);
        if (includes != null) {
            sb.append(",includes=").append(includes);
        }
        return sb.toString();
    }

}
