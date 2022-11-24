package com.sos.js7.converter.js1.output.js7.helper;

import com.sos.js7.converter.js1.common.job.OrderJob;

public class JobChainJobHelper {

    private OrderJob job;
    private String js7JobName;

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
}
