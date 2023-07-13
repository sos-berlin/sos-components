package com.sos.jitl.jobs.examples;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.JobArguments;
import com.sos.commons.job.OrderProcessStep;

public class EmptyJob extends ABlockingInternalJob<JobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
        
    }

}
