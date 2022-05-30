package com.sos.js7.converter.js1.common.jobchain.node;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNodeJobChain extends AJobChainNode {

    private static final String ATTR_JOB_CHAIN = "job_chain";
    private static final String ATTR_STATE = "state";
    private static final String ATTR_NEXT_STATE = "next_state";
    private static final String ATTR_ERROR_STATE = "error_state";

    private String jobChain; // job chain name
    private String state;
    private String nextState;
    private String errorState;

    protected JobChainNodeJobChain(Node node, JobChainNodeType type) {
        super(node, type);
        jobChain = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_JOB_CHAIN));
        state = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_STATE));
        nextState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_NEXT_STATE));
        errorState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_ERROR_STATE));
    }

    public String getJobChain() {
        return jobChain;
    }

    public String getState() {
        return state;
    }

    public String getNextState() {
        return nextState;
    }

    public String getErrorState() {
        return errorState;
    }

}
