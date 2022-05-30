package com.sos.js7.converter.js1.common.jobchain.node;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNodeFileOrderSink extends AJobChainNode {

    private static final String ATTR_MOVE_TO = "move_to";
    private static final String ATTR_REMOVE = "remove";
    private static final String ATTR_STATE = "state";

    private String moveTo; // directory path
    private Boolean remove; // yes|no
    private String state;

    protected JobChainNodeFileOrderSink(Node node, JobChainNodeType type) {
        super(node, type);
        moveTo = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_MOVE_TO));
        remove = JS7ConverterHelper.booleanValue(getAttributes().get(ATTR_REMOVE));
        state = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_STATE));
    }

    public String getMoveTo() {
        return moveTo;
    }

    public Boolean getRemove() {
        return remove;
    }

    public String getState() {
        return state;
    }

}
