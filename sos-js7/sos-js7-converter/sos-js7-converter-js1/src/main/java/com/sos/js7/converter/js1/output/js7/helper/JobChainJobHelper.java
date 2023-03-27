package com.sos.js7.converter.js1.output.js7.helper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sos.inventory.model.job.Environment;
import com.sos.js7.converter.js1.common.job.OrderJob;

public class JobChainJobHelper {

    private OrderJob job;
    private String js7JobName;
    private Set<String> js7JobNodesDefaultArguments = new HashSet<>();

    public JobChainJobHelper(OrderJob job, String js7JobName) {
        this.job = job;
        this.js7JobName = js7JobName;
    }

    public OrderJob getJob() {
        return job;
    }

    public String getJS7JobName() {
        return js7JobName;
    }

    public void setJS7JobNodesDefaultArguments(Environment env) {
        if(env == null || env.getAdditionalProperties().size() == 0) {
            return;
        }
        for (Map.Entry<String, String> e : env.getAdditionalProperties().entrySet()) {
            if (!js7JobNodesDefaultArguments.contains(e.getKey())) {
                js7JobNodesDefaultArguments.add(e.getKey());
            }
        }
    }

    public Set<String> getJS7JobNodesDefaultArguments() {
        return js7JobNodesDefaultArguments;
    }
}
