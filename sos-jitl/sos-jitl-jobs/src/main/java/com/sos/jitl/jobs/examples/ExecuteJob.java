package com.sos.jitl.jobs.examples;

import com.sos.js7.job.Job;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.OrderProcessStep;

public class ExecuteJob extends Job<JobArguments> {

    public ExecuteJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<JobArguments> step) throws Exception {
        step.getLogger().info("calling JITL com.sos.jitl.jobs.examples.InfoJob");
        step.executeJob(com.sos.jitl.jobs.examples.InfoJob.class);
    }

}
