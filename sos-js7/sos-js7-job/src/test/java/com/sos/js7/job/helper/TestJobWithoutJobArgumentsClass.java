package com.sos.js7.job.helper;

import com.sos.js7.job.Job;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.OrderProcessStep;

public class TestJobWithoutJobArgumentsClass extends Job<JobArguments> {

    public TestJobWithoutJobArgumentsClass(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<JobArguments> step) throws Exception {
    }
}
