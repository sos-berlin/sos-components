package com.sos.js7.converter.js1.common.jobchain.node;

import java.nio.file.Path;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNodeEnd extends AJobChainNode {

    private static final String ATTR_STATE = "state";

    private String state;

    protected JobChainNodeEnd(Path jobChainPath, JobChainNodeType type, Node node) {
        super(jobChainPath, type,node);
        state = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_STATE));
    }

    public String getState() {
        return state;
    }

}
