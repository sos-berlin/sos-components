package com.sos.js7.converter.js1.output.js7.helper;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNode;

public class JobChainStateHelper {

    private final String js1State;
    private final String js1NextState;
    private final String js1ErrorState;
    private final String js1JobName;

    private final String js7State;
    private final String js7JobName;

    private final String onError;

    public JobChainStateHelper(JobChainNode node, String js1JobName, String js7JobName) {
        this.js1State = node.getState();
        this.js1NextState = node.getNextState() == null ? "" : node.getNextState();
        this.js1ErrorState = node.getErrorState() == null ? "" : node.getErrorState();
        this.js1JobName = js1JobName;
        this.js7JobName = js7JobName;

        this.js7State = getJS7Name(node, this.js1State);

        this.onError = node.getOnError() == null ? "" : node.getOnError();
    }

    private String getJS7Name(JobChainNode node, String val) {
        if (SOSString.isEmpty(val)) {
            return null;
        }
        return JS7ConverterHelper.getJS7ObjectName(node.getPath(), val);
    }

    public String getJS1State() {
        return js1State;
    }

    public String getJS1NextState() {
        return js1NextState;
    }

    public String getJS1ErrorState() {
        return js1ErrorState;
    }

    public String getJS1JobName() {
        return js1JobName;
    }

    public String getJS7State() {
        return js7State;
    }

    public String getJS7JobName() {
        return js7JobName;
    }

    public String getOnError() {
        return onError;
    }

}
