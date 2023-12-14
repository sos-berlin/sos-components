package com.sos.js7.converter.js1.common.commands;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.Params;

public class AddOrder {

    private static final String ELEMENT_PARAMS = "params";

    private static final String ATTR_ID = "id";
    private static final String ATTR_XMLNS = "xmlns";
    private static final String ATTR_JOB_CHAIN = "job_chain";

    private Params params;
    private final String id;
    private final String xmlns;
    private final String jobChain;

    public AddOrder(Node node) throws Exception {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        id = JS7ConverterHelper.stringValue(m.get(ATTR_ID));
        xmlns = JS7ConverterHelper.stringValue(m.get(ATTR_XMLNS));
        jobChain = JS7ConverterHelper.stringValue(m.get(ATTR_JOB_CHAIN));

        SOSXMLXPath xpath = SOSXML.newXPath();
        Node params = xpath.selectNode(node, "./" + ELEMENT_PARAMS);
        if (params != null) {
            this.params = new Params(xpath, params);
        }
    }

    public Params getParams() {
        return params;
    }

    public String getId() {
        return id;
    }

    public String getXMLNS() {
        return xmlns;
    }

    public String getJobChain() {
        return jobChain;
    }

    public boolean hasParams() {
        return params != null && params.hasParams();
    }

    public boolean hasCopyParams() {
        return params != null && params.hasCopyParams();
    }
}
