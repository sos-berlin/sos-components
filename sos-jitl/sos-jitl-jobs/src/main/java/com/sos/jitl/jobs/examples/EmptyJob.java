package com.sos.jitl.jobs.examples;

import com.sos.js7.job.Job;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.OrderProcessStep;

public class EmptyJob extends Job<JobArguments> {

    @Override
    public void processOrder(OrderProcessStep<JobArguments> step) throws Exception {

    }

}
