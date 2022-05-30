package com.sos.js7.converter.js1.common.jobchain.node;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNodeEnd extends AJobChainNode {

    private static final String ATTR_STATE = "state";

    private String state;

    protected JobChainNodeEnd(Node node, JobChainNodeType type) {
        super(node, type);
        state = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_STATE));
    }

    public String getState() {
        return state;
    }

}
