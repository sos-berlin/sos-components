package com.sos.js7.converter.js1.common.jobchain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.Params;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class JobChainConfig {

    private static final String ELEMENT_PARAMS = "params";
    private static final String ELEMENT_ORDER_PARAMS = "job_chain/order/" + ELEMENT_PARAMS;
    private static final String ELEMENT_ORDER_PROCESS = "job_chain/order/process";

    private static final String ATTR_STATE = "state";

    private Path path;// extra

    private Params orderParams;
    private Map<String, Params> process;

    public JobChainConfig(DirectoryParserResult pr, Path file) throws Exception {
        this.path = file;
        Node node = JS7ConverterHelper.getDocumentRoot(file);

        SOSXMLXPath xpath = SOSXML.newXPath();
        Node params = xpath.selectNode(node, "./" + ELEMENT_ORDER_PARAMS);
        if (params != null) {
            this.orderParams = new Params(xpath, params);
        }
        this.process = getProcess(xpath, node);
    }

    public boolean hasOrderParams() {
        return orderParams != null && orderParams.hasParams();
    }

    public boolean hasProcess() {
        return process != null && process.size() > 0;
    }

    private Map<String, Params> getProcess(SOSXMLXPath xpath, Node rootNode) throws Exception {
        Map<String, Params> result = null;
        NodeList nl = xpath.selectNodes(rootNode, "./" + ELEMENT_ORDER_PROCESS);
        if (nl != null && nl.getLength() > 0) {
            result = new HashMap<>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                NamedNodeMap m = node.getAttributes();
                if (m != null) {
                    String state = JS7ConverterHelper.getAttributeValue(m, ATTR_STATE);
                    Node params = xpath.selectNode(node, "./" + ELEMENT_PARAMS);
                    if (state != null && params != null) {
                        result.put(state, new Params(xpath, params));
                    }
                }
            }
        }
        return result;
    }

    public Path getPath() {
        return path;
    }

    public Params getOrderParams() {
        return orderParams;
    }

    public Map<String, Params> getProcess() {
        return process;
    }
}
