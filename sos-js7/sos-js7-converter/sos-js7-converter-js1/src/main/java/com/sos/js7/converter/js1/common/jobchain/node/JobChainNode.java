package com.sos.js7.converter.js1.common.jobchain.node;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobChainNode extends AJobChainNode {

    private static final String ATTR_JOB = "job";
    private static final String ATTR_STATE = "state";
    private static final String ATTR_NEXT_STATE = "next_state";
    private static final String ATTR_ERROR_STATE = "error_state";
    private static final String ATTR_ON_ERROR = "on_error";
    private static final String ATTR_DELAY = "delay";

    private String job; // job_name
    private String state;
    private String nextState;
    private String errorState;

    private String onError; // suspend|setback
    private String delay; // seconds
    // TODO on_return_codes

    protected JobChainNode(Node node, JobChainNodeType type) {
        super(node, type);
        job = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_JOB));
        state = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_STATE));
        nextState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_NEXT_STATE));
        errorState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_ERROR_STATE));
        onError = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_ON_ERROR));
        delay = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_DELAY));
    }

    public String getJob() {
        return job;
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

    public String getOnError() {
        return onError;
    }

    public String getDelay() {
        return delay;
    }

}
